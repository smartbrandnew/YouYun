package com.broada.carrier.monitor.util;

public class OsInfoUtil {
	
	/**
	 * 获取本机操作系统，目前只区分linux和windows
	 * @return
	 */
	public static String getOS(){
		String osName = System.getProperty("os.name");
		if(osName.toLowerCase().contains("windows"))
			return "windows";
		else
			return "linux";
	}
	
}
