package uyun.bat.agent.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.common.spring.SpringStartup;
import uyun.whale.common.util.error.ErrorUtil;

public class Startup extends SpringStartup {
	private static Logger logger = LoggerFactory.getLogger(Startup.class);
	private static Startup instance;

	/**
	 * 获取默认单例
	 * 
	 * @return 单例
	 */
	public static Startup getInstance() {
		if (instance == null) {
			synchronized (Startup.class) {
				if (instance == null)
					instance = new Startup();
			}
		}
		return instance;
	}

	public Startup() {
		super("agent", "classpath:uyun/bat/agent/impl/spring.xml");
	}

	public static void main(String[] args) {
		getInstance().startup();
		// getInstance().waitExit();
		// getInstance().shutdown();
	}

	private void waitExit() {
		logger.info(getName() + " runing...");
		logger.info("Input any key exit: ");
		try {
			System.in.read();
		} catch (IOException e) {
			ErrorUtil.warn(logger, "input error", e);
		}
	}
}
