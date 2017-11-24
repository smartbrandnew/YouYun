package com.broada.carrier.monitor.probe.impl.util;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.common.util.HostIpUtil;

public class HostUtil {
	private static Logger logger = LoggerFactory.getLogger(HostUtil.class);
	
	public static String getIP() {
		String ip = HostIpUtil.getLocalHost();
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
			if (ip == null)
				ip = HostIpUtil.getLocalHost();
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
