package uyun.bat.syndatabase.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpUtil {
	
	/**
	 * 判断Ip地址是否符合规范
	 * @param ipAddress
	 * @return 
	 */
	public static boolean isIp(String ipAddress){
		String ip ="^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$";
		Pattern pattern = Pattern.compile(ip);
		Matcher matcher = pattern.matcher(ipAddress);
		return matcher.matches();
	}
	
}
