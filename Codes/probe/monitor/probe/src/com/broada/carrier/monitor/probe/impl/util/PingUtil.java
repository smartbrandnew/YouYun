package com.broada.carrier.monitor.probe.impl.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingUtil {

	private static final Logger LOG = LoggerFactory.getLogger(PingUtil.class);

	/**
	 * 监测监测主机可达性
	 * @param ip
	 * @param pingTimes
	 * @param os
	 * @return
	 */
	public static boolean ping(String ip, int pingTimes) {  
		BufferedReader in = null;  
		Runtime r = Runtime.getRuntime();
		String pingCommand = "";
		int connectedCount = 0;
		String os = getOS();
		if(os.equals("windows"))
			pingCommand = "ping " + ip + " -n " + pingTimes + " -w " + 10;  
		else if(os.equals("linux"))
			pingCommand = "ping " + ip + " -c " + pingTimes + " -w " + 10; 
		try {
			Process p = r.exec(pingCommand);  
			if (p == null) return false;
			in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;   
			while ((line = in.readLine()) != null)  
				connectedCount += getCheckResult(line);
			return connectedCount == pingTimes;  
		} catch (Exception ex) {
			LOG.error("ping" + ip + "时发生错误");
			return false;  
		} finally {   
			try {    
				in.close();   
			} catch (Exception e) {    
				LOG.error("关闭IO流异常");  
			}
		}
	}
	
	private static int getCheckResult(String line) {
		Pattern pattern = Pattern.compile("[TTL|ttl]=\\d+", Pattern.CASE_INSENSITIVE);  
        Matcher matcher = pattern.matcher(line);  
        while (matcher.find()) {
            return 1;
        }
        return 0; 
    }
	
	/**
	 * 获取jdk所在操作系统类别
	 * @return
	 */
	private static String getOS(){
		String osName = System.getProperty("os.name");
		if(osName.toLowerCase().contains("windows"))
			return "windows";
		else
			return "linux";
	}

}
