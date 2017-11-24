package com.broada.carrier.monitor.probe.impl.util;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostUtil {
	private static Logger logger = LoggerFactory.getLogger(HostUtil.class);
	
	public static String getIP() {
		String ip = "127.0.0.1";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
			if (ip == null)
				ip = "127.0.0.1";
		} catch (Exception e) {
			logger.warn("获取主机IP信息异常: " + e);
		}
		return ip;
	}

	public static String getHostname() {
		String name = "localhost";
		try {
			name = InetAddress.getLocalHost().getHostName();
			if (name == null)
				name = "localhost";
		} catch (Exception e) {
			logger.warn("获取主机IP信息异常: " + e);
		}
		return name;
	}
}
