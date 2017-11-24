package com.broada.carrier.monitor.common.net;

import java.math.BigInteger;
import java.util.List;

/**
 * IP网段处理相关的工具类。<br>
 * 合法的IPv4网段表示形式如下所示(IPv6类似)：<br>
 * <ul>
 * <li>单个IP地址，例如：192.168.0.1</li>
 * <li>单个IP地址 + '/' + 数字x（0 <= x <= 32），例如：192.168.0.1/24</li>
 * <li>单个IP地址 + '/' + 子网掩码,例如：192.168.0.1/255.255.255.0</li>
 * <li>单个IP地址 + '~' + 单个IP地址,例如：192.168.0.1~192.168.254（~符号前后允许存在空格）</li>
 * </ul>
 */
public class IPUtil {

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
		return ipSegment.contains(":") ? IPv6Util.isValidate(ipSegment) : IPv4Util.isValidate(ipSegment);
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
		try {
			return ipSegment.contains(":") ? IPv6Util.countAddress(ipSegment) : IPv4Util.countAddress(ipSegment);
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
		return ipSegment.contains(":") ? IPv6Util.listIPs(ipSegment) : IPv4Util.listIPs(ipSegment);
	}

	/**
	 * 将长整型的IP地址转换为字符串形式的IP地址。
	 */
	public static String transLongIpToStringIp(BigInteger ip) {
		String addr = IPv4Util.transLongIpToStringIp(ip.longValue());
		return IPv4Util.isIPv4Address(addr) ? addr : IPv6Util.transLongIpToStringIp(ip);
	}

	/**
	 * 将字符串形式的IP地址转换为长整型IP地址。
	 */
	public static BigInteger transStringIpToLongIp(String ip) {
		if (ip == null || ip.isEmpty()) {
			return BigInteger.ZERO;
		}
		if (ip.contains(":")) {
			IPv6Util.transStringIpToLongIp(ip);
		}
		return BigInteger.valueOf(IPv4Util.transStringIpToLongIp(ip));
	}

	/**
	 * 判断一个地址是否是合法的IP地址。
	 */
	public static boolean isIPAddress(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}
		return str.contains(":") ? IPv6Util.isIPv6Address(str) : IPv4Util.isIPv4Address(str);
	}
	
	/**
	 * 判断一个地址是否是IP V4版本的地址。
	 * 
	 * @param ip IP地址
	 * @return 如果是返回true，否则返回false
	 */
	public static boolean isIPv4Address(String ip) {
		return IPv4Util.isIPv4Address(ip);
	}
	
	/**
	 * 判断一个地址是否是IP V6版本的地址。
	 * 
	 * @param ip IP地址
	 * @return 如果是返回true，否则返回false
	 */
	public static boolean isIPv6Address(String ip) {
		return IPv6Util.isIPv6Address(ip);
	}
}
