package com.broada.carrier.monitor.common.util;

import java.util.Comparator;

public class IPUtil {
	public static Comparator<String> IP_COMPARATOR = new IPComparator();

	public static long parse(String strip) {
		// 定义正则表达式
		String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
		// 判断ip地址是否与正则表达式匹配
		if (!strip.matches(regex))
			throw new IllegalArgumentException(String.format("[%s]不是一个合法的IP地址", strip));
		try {
			long[] ip = new long[4];
			int position1 = strip.indexOf(".");
			int position2 = strip.indexOf(".", position1 + 1);
			int position3 = strip.indexOf(".", position2 + 1);
			ip[0] = Long.parseLong(strip.substring(0, position1));
			ip[1] = Long.parseLong(strip.substring(position1 + 1, position2));
			ip[2] = Long.parseLong(strip.substring(position2 + 1, position3));
			ip[3] = Long.parseLong(strip.substring(position3 + 1));
			return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3]; // ip1*256*256*256+ip2*256*256+ip3*256+ip4
		} catch (Throwable e) {
			throw new IllegalArgumentException(String.format("[%s]不是一个合法的IP地址", strip));
		}
	}

	public static String toString(long longip) {
		StringBuilder sb = new StringBuilder("");
		sb.append(String.valueOf(longip >>> 24));// 直接右移24位
		sb.append(".");
		sb.append(String.valueOf((longip & 0x00ffffff) >>> 16)); // 将高8位置0，然后右移16位
		sb.append(".");
		sb.append(String.valueOf((longip & 0x0000ffff) >>> 8));
		sb.append(".");
		sb.append(String.valueOf(longip & 0x000000ff));
		return sb.toString();
	}

	public static class IPComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			return (int) (IPUtil.parse(o1) - IPUtil.parse(o2));
		}
	}
	
}
