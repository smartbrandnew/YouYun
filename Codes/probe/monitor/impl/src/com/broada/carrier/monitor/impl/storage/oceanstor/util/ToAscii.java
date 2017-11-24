package com.broada.carrier.monitor.impl.storage.oceanstor.util;

public class ToAscii {
	
	public static final String PREFIX_TABLE_ID = ".1.3.6.1.4.1.2011.2.251.36.5.5.1.1";
	public static final String DISK_SLOT_INDEX = "10";
	public static final String SECTION_COUNT_INDEX = "10";
	public static final String SECTION_SIZE_INDEX = "11";
	public static final String SECTION_BANDWIDTH_INDEX = "12";
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String String2Ascii(String str){
		byte[] bytes = str.getBytes();
		char[] chars = new char[bytes.length];
		StringBuffer sb = new StringBuffer();
		for(int i=0; i< chars.length;i++){
			sb.append((int)bytes[i]).append(".");
		}
		return sb.substring(0, sb.length()-1).toString();
	}
	
	/**
	 * 拼接字符串
	 * @param oid_prefix
	 * @param index
	 * @param length
	 * @param ascii
	 */
	public static String getOid(String oid_prefix, String index, String length, String ascii){
		StringBuffer sb = new StringBuffer();
		sb.append(oid_prefix).append(".").append(index)
		  .append(".").append(length).append(".").append(ascii);
		return sb.toString();
	}
}
