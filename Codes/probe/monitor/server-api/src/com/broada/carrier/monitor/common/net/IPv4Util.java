package com.broada.carrier.monitor.common.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.broada.carrier.monitor.common.net.SubnetUtil.SubnetInfo;

/**
 * 定义IPV4操作相关的工具方法。
 */
class IPv4Util {

	// 字符串形式的 IPV4 地址
	private static final String IP_ADDRESS_V4 = "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])";

	// IP网段表示形式：192.168.0.1
	private static final String SEGMENT_FORMAT_0 = "^" + IP_ADDRESS_V4 + "$";

	// IP网段表示形式：192.168.0.1/24
	private static final String SEGMENT_FORMAT_1 = "^" + IP_ADDRESS_V4 + "/(\\d{1,3})" + "$";

	// IP网段表示形式：192.168.0.1 ~ 192.168.0.5
	private static final String SEGMENT_FORMAT_2 = "^" + IP_ADDRESS_V4 + "\\s*~\\s*" + IP_ADDRESS_V4 + "$";

	private static final Pattern PATTERN_0 = Pattern.compile(SEGMENT_FORMAT_0);
	private static final Pattern PATTERN_1 = Pattern.compile(SEGMENT_FORMAT_1);
	private static final Pattern PATTERN_2 = Pattern.compile(SEGMENT_FORMAT_2);

	/**
	 * 判断指定的IP网段表示知否合法。
	 * 
	 * @param ipSegment IP网段
	 * @return 如果合法返回true，否则返回false
	 */
	public static boolean isValidate(String ipSegment) {
		if (ipSegment == null) {
			return false;
		}
		ipSegment = ipSegment.trim();
		if (ipSegment.isEmpty()) {
			return false;
		}
		if (PATTERN_0.matcher(ipSegment).matches()) {
			return true;
		}
		Matcher matcher = PATTERN_1.matcher(ipSegment);
		if (matcher.matches()) {
			int mask = Integer.parseInt(matcher.group(5));
			return mask >= 0 && mask <= 32;
		}
		// 区间形式
		matcher = PATTERN_2.matcher(ipSegment);
		if (matcher.matches()) {
			return countRangeSengment(ipSegment, matcher) > 0;
		}
		return false;
	}

	/**
	 * 计算指定IP网段所包含的IP地址数。
	 * 
	 * @param ipSegment IP网段
	 * @return IP地址数
	 */
	public static long countAddress(String ipSegment) {
		if (ipSegment == null) {
			return 0;
		}
		ipSegment = ipSegment.trim();
		if (ipSegment.isEmpty()) {
			return 0;
		}
		// TODO 需要排除广播地址和网段地址
		if (PATTERN_0.matcher(ipSegment).matches()) {
			return 1;
		}
		Matcher matcher = PATTERN_2.matcher(ipSegment);
		if (matcher.matches()) {
			return countRangeSengment(ipSegment, matcher);
		}
		try {
			SubnetInfo netInfo = new SubnetUtil(ipSegment).getInfo();
			return netInfo.getAddressCount();
		} catch (Exception e) {
		}
		return 0L;
	}

	/**
	 * 返回指定IP网段下所有可用的IP地址（排除了网段地址和广播地址）。
	 * 
	 * @param ipSegment IP网段
	 * @return IP地址列表，当网段不合法时返回null
	 */
	public static List<String> listIPs(String ipSegment) {
		if (ipSegment == null) {
			return null;
		}
		ipSegment = ipSegment.trim();
		if (ipSegment.isEmpty()) {
			return null;
		}

		List<String> list = new ArrayList<String>();
		// TODO 需要排除广播地址和网段地址
		if (PATTERN_0.matcher(ipSegment).matches()) {
			list.add(ipSegment);
			return list;
		}
		Matcher matcher = PATTERN_2.matcher(ipSegment);
		if (matcher.matches()) {
			return listRangeSengment(ipSegment, matcher);
		}
		try {
			SubnetInfo netInfo = new SubnetUtil(ipSegment).getInfo();
			return Arrays.asList(netInfo.getAllAddresses());
		} catch (Exception e) {
		}
		return null;
	}

	/*
	 * 统计区间形式的网段内的IP数目。
	 */
	private static long countRangeSengment(String ipSegment, Matcher matcher) {
		return listRangeSengment(ipSegment, matcher).size();
	}

	/*
	 * 先简单实现。
	 */
	private static List<String> listRangeSengment(String ipSegment, Matcher matcher) {
		matcher.matches();
		int s1 = Integer.parseInt(matcher.group(1));
		int s2 = Integer.parseInt(matcher.group(2));
		int s3 = Integer.parseInt(matcher.group(3));
		int s4 = Integer.parseInt(matcher.group(4));
		int e1 = Integer.parseInt(matcher.group(5));
		int e2 = Integer.parseInt(matcher.group(6));
		int e3 = Integer.parseInt(matcher.group(7));
		int e4 = Integer.parseInt(matcher.group(8));

		final String fomatter = "%d.%d.%d.%d";
		String start = String.format(fomatter, s1, s2, s3, s4);
		String end = String.format(fomatter, e1, e2, e3, e4);

		List<String> result = new ArrayList<String>();
		long startIp = transStringIpToLongIp(start);
		long endIp = transStringIpToLongIp(end);

		for (long i = startIp; i <= endIp; i++) {
			String ip = transLongIpToStringIp(i);
			if (isIPv4Address(ip)) {
				result.add(ip);
			}
		}
		return result;
	}

	/**
	 * 将长整型的IP地址转换为字符串形式的IP地址。
	 */
	public static String transLongIpToStringIp(long ip) {
		StringBuilder result = new StringBuilder(String.valueOf((int) (ip & 255)));
		for (int i = 1; i <= 3; i++) {
			result.insert(0, ".").insert(0, (int) ((ip >> i * 8) & 255));
		}
		return result.toString();
	}

	/**
	 * 将字符串形式的IP地址转换为长整型IP地址。
	 */
	public static long transStringIpToLongIp(String ip) {
		if (ip == null) {
			return 0L;
		}
		long result = 0;
		if (!isIPv4Address(ip))
			return 0;
		for (String str : ip.split("\\.")) {
			result = (result << 8) + Long.parseLong(str);
		}
		return result;
	}

	/**
	 * 判断一个地址是否是IP地址。
	 */
	public static boolean isIPv4Address(String str) {
		if (str == null) {
			return false;
		}
		Matcher matcher = PATTERN_0.matcher(str.trim());
		return matcher.matches();
	}

}
