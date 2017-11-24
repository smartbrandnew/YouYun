package com.broada.carrier.monitor.impl.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.utils.PingUtil;
import com.broada.utils.ShellUtil;

public class LinuxPingUtil {
	private static final Log logger = LogFactory.getLog(PingUtil.class);

	private static String PING_PATH = "ping";
	private static int packagesize = 56;

	public static int ping(String ipAddr, int timeout, int times, int interval)
				throws IOException {
		if (ipAddr == null) {
			throw new NullPointerException("目标地址(ipAddr)不能为空(null).");
		}
		ipAddr = ipAddr.trim();
		boolean ok = verifyAddr(ipAddr);
		if (!(ok)) {
			throw new IllegalArgumentException("地址参数:" + ipAddr + "不能包含空格.");
		}
		String cmd = PING_PATH + " " + ipAddr + " -c " + times + " -s " + packagesize;
		String rel = "";
		try {
			try {
				rel = ProcessUtil.readOutputUntilTerminal(ShellUtil.execProgram(cmd));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (rel == null) {
				return -1;
			}
			int avgTime = assay(rel, ipAddr, times, timeout);
			if ((avgTime < 0) && (logger.isDebugEnabled())) {
				logger.info("Ping结果如下:\n" + rel);
			}
			return avgTime;
		} catch (IOException e) {
			throw e;
		}
	}

	private static int assay(String result, String ipAddr, int times, int timeout)
				throws IOException {
		String str = (packagesize + 8) + " bytes from " + ipAddr + ":";
		BufferedReader br = new BufferedReader(new StringReader(result));
		String rel = null;
		int successCount = 0;
		double allTime = 0;
		while ((rel = br.readLine()) != null) {
			if (rel.contains(str)) {
				String[] args = rel.split("time=");
				args[1] = args[1].substring(0, 5);
				double time = Double.parseDouble(args[1]);
				if (time < timeout) {
					successCount++;
					allTime += time;
				}
			}
		}
		if (successCount == 0) {
			return -1;
		}
		double avgTime = allTime / successCount;
		return (int)avgTime*1000;
	}

	private static boolean verifyAddr(String ipAddr) {
		return (ipAddr.indexOf(32) < 0);
	}
}
