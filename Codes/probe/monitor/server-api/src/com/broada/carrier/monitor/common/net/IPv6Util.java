package com.broada.carrier.monitor.common.net;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.broada.carrier.monitor.common.net.ipv6.IPv6Address;
import com.broada.carrier.monitor.common.net.ipv6.IPv6AddressRange;
import com.broada.carrier.monitor.common.net.ipv6.IPv6Network;

/**
 * 定义IPv4操作相关的工具方法。
 */
class IPv6Util {

	/**
	 * 判断指定的IP网段表示知否合法。
	 * 
	 * @param ipSegment IP网段
	 * @return 如果合法返回true，否则返回false
	 */
	public static boolean isValidate(String ipSegment) {
		if (ipSegment == null || ipSegment.isEmpty()) {
			return false;
		}
		// 起止区间形式
		if (ipSegment.contains("~")) {
			String[] ipAddrs = ipSegment.split("~");
			if (ipAddrs.length != 2) {
				return false;
			}
			IPv6Address first = toIPv6Address(ipAddrs[0]);
			IPv6Address last = toIPv6Address(ipAddrs[1]);
			return first != null && last != null && first.compareTo(last) <= 0;
		}
		// 子网掩码形式
		if (ipSegment.contains("/")) {
			try {
				return IPv6Network.fromString(ipSegment) != null;
			} catch (Exception e) {
				return false;
			}
		}
		// 单个地址
		return isIPv6Address(ipSegment);
	}

	/**
	 * 计算指定IP网段所包含的IP地址数。
	 * 
	 * @param ipSegment IP网段
	 * @return IP地址数
	 */
	public static long countAddress(String ipSegment) {
		if (ipSegment == null || ipSegment.isEmpty()) {
			return 0;
		}
		// 起止区间形式
		if (ipSegment.contains("~")) {
			String[] ipAddrs = ipSegment.split("~");
			if (ipAddrs.length != 2) {
				return 0;
			}
			IPv6Address first = toIPv6Address(ipAddrs[0]);
			IPv6Address last = toIPv6Address(ipAddrs[1]);
			if (first == null || last == null || first.compareTo(last) > 0) {
				return 0;
			}
			BigInteger size = IPv6AddressRange.fromFirstAndLast(first, last).size();
			// TODO 最多为long的MAX_VALUE个
			return size.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ? 0 : size.longValue();
		}
		// 子网掩码形式
		if (ipSegment.contains("/")) {
			try {
				BigInteger size = IPv6Network.fromString(ipSegment).size();
				return size.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ? 0 : size.longValue();
			} catch (Exception e) {
				return 0L;
			}
		}
		// 单个地址
		return isIPv6Address(ipSegment) ? 1L : 0L;
	}

	/**
	 * 返回指定IP网段下所有可用的IP地址（排除了网段地址和广播地址）。
	 * 
	 * @param ipSegment IP网段
	 * @return IP地址列表，当网段不合法时返回null
	 */
	public static List<String> listIPs(String ipSegment) {
		if (ipSegment == null || ipSegment.isEmpty()) {
			return null;
		}
		// 起止区间形式
		if (ipSegment.contains("~")) {
			String[] ipAddrs = ipSegment.split("~");
			if (ipAddrs.length != 2) {
				return null;
			}
			IPv6Address first = toIPv6Address(ipAddrs[0]);
			IPv6Address last = toIPv6Address(ipAddrs[1]);
			if (first == null || last == null || first.compareTo(last) > 0) {
				return null;
			}
			IPv6AddressRange range = IPv6AddressRange.fromFirstAndLast(first, last);
			// TODO 最多为long的MAX_VALUE个
			if (range.size().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
				return null;
			}
			List<String> ipList = new ArrayList<String>();
			for (Iterator<IPv6Address> it = range.iterator(); it.hasNext();) {
				ipList.add(it.next().toString());
			}
			return ipList;
		}
		// 子网掩码形式
		if (ipSegment.contains("/")) {
			try {
				IPv6Network network = IPv6Network.fromString(ipSegment);
				// TODO 最多为long的MAX_VALUE个
				if (network.size().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
					return null;
				}
				List<String> ipList = new ArrayList<String>();
				for (Iterator<IPv6Address> it = network.iterator(); it.hasNext();) {
					ipList.add(it.next().toString());
				}
				return ipList;
			} catch (Exception e) {
				return null;
			}
		}
		// 单个地址
		if (isIPv6Address(ipSegment)) {
			return Arrays.asList(ipSegment);
		}
		return null;
	}

	/**
	 * 将长整型的IP地址转换为字符串形式的IP地址。
	 */
	public static String transLongIpToStringIp(BigInteger ip) {
		try {
			return IPv6Address.fromBigInteger(ip).toString();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 将字符串形式的IP地址转换为长整型IP地址。
	 */
	public static BigInteger transStringIpToLongIp(String ip) {
		if (ip == null || ip.isEmpty()) {
			return BigInteger.ZERO;
		}
		IPv6Address ipv6Addr = toIPv6Address(ip);
		return ipv6Addr == null ? BigInteger.ZERO : ipv6Addr.toBigInteger();
	}

	private static IPv6Address toIPv6Address(String ip) {
		try {
			return IPv6Address.fromString(ip);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 判断一个地址是否是IP地址。
	 */
	public static boolean isIPv6Address(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}
		return toIPv6Address(str) != null;
	}
}
