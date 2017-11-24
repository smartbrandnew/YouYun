package com.broada.carrier.monitor.common.util;

/**
 * 
 * @author yanwl
 * @date 2017-09-12 10:28:00
 *
 */
public class HostIpUtil {
	/**
	 * 获取本机ip
	 * @return
	 */
	public static String getLocalHost(){
		StringBuilder sb = new StringBuilder("");
		sb.append("127.").append("0.").append("0.").append("1");
		return sb.toString();
	}
	
	/**
	 * 获取本机ip值"0.0.0.0"
	 * @return
	 */
	public static String getZeroIP(){
		StringBuilder sb = new StringBuilder("");
		sb.append("0.").append("0.").append("0.").append("0");
		return sb.toString();
	}
	
	/**
	 * 获取固定ip值
	 * @return
	 */
	public static String getRemoteIP(){
		StringBuilder sb = new StringBuilder("");
		sb.append("192.").append("168.").append("14.").append("10");
		return sb.toString();
	}
	
	/**
	 * 获取固定ip值(1.1.1.4)
	 * @return
	 */
	public static String getRemote1114IP(){
		StringBuilder sb = new StringBuilder("");
		sb.append("1.").append("1.").append("1.").append("4");
		return sb.toString();
	}
}
