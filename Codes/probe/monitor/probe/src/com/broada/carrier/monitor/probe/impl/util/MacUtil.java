package com.broada.carrier.monitor.probe.impl.util;

import java.net.InetAddress;
import java.net.NetworkInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.common.util.HostIpUtil;
import com.broada.carrier.monitor.probe.impl.config.Config;

public class MacUtil {

	private static final Logger logger = LoggerFactory.getLogger(MacUtil.class);

	public static String getMacAddr() {
		String ip = Config.getDefault().getProperty("probe.ipaddr", HostIpUtil.getLocalHost());
		if (HostIpUtil.getLocalHost().equals(ip))
			throw new RuntimeException("config.properties文件中属性probe.ipaddr不能配置为"+HostIpUtil.getLocalHost());
		String macAddr = getMacAddr(ip);
		return macAddr;
	}

	private static String getMacAddr(String ip) {
		String macIpaddr = null;
		try {
			InetAddress inetAddress = InetAddress.getByName(ip);
			NetworkInterface networknterface = NetworkInterface.getByInetAddress(inetAddress);
			if (networknterface != null) {
				byte[] bytes = networknterface.getHardwareAddress();
				if (bytes == null)
					throw new RuntimeException("mac地址获取失败,网卡名称:" + networknterface.getName());
				StringBuffer sb = new StringBuffer("");
				for (int i = 0; i < bytes.length; i++) {
					if (i != 0) {
						sb.append("-");
					}
					// 字节转换为整数
					int temp = bytes[i] & 0xff;
					String str = Integer.toHexString(temp);
					if (str.length() == 1) {
						sb.append("0" + str);
					} else {
						sb.append(str);
					}
				}
				macIpaddr = sb.toString().toUpperCase();
			}
		} catch (Exception e) {
			logger.warn("获取mac地址异常: ", e);
		}

		return macIpaddr;
	}
}
