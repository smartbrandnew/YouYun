package com.broada.carrier.monitor.probe.impl.db;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;

import org.apache.derby.drda.NetworkServerControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.probe.impl.WebStartupListener;
import com.broada.component.utils.error.ErrorUtil;

public class DerbyServer {
	private static Logger logger;
	private NetworkServerControl serverControl;
	private String ip = "localhost";
	private int port = 9147;

	static {
		WebStartupListener.checkSystemProperties();
		logger = LoggerFactory.getLogger(DerbyServer.class);
	}

	public DerbyServer(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public DerbyServer(int port) {
		this("127.0.0.1", port);
	}

	private void initialize() throws Exception {
		serverControl = new NetworkServerControl(InetAddress.getByName(ip), port);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				try {
					serverControl.ping();
					serverControl.shutdown();
				} catch (Exception e) {
					ErrorUtil.warn(logger, "Derby关闭失败", e);						
				}
			}
		}));
	}

	public void shutdown() throws Exception {
		serverControl.shutdown();
	}

	public void startup() throws Exception {
		initialize();
		serverControl.start(LogPrintWriter.create());
		while (true) {				
			try {
				serverControl.ping();
				break;
			} catch (Exception e) {
				ErrorUtil.warn(logger, "Derby ping失败", e);
			}
		}
	}

	private static class LogPrintWriter extends PrintWriter {
		private StringWriter sw;

		private LogPrintWriter(StringWriter sw) {
			super(sw, true);
			this.sw = sw;
		}

		public static LogPrintWriter create() {
			return new LogPrintWriter(new StringWriter());
		}

		@Override
		public void flush() {
			logger.debug(sw.getBuffer().toString());
			sw.getBuffer().setLength(0);
			super.flush();
		}

		@Override
		public void close() {
			flush();
			super.close();
		}
	}
}
