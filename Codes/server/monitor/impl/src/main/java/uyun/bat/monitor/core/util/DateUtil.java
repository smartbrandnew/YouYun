package uyun.bat.monitor.core.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DateUtil {
	private static final String dataTimeFormat = "yyyy-MM-dd HH:mm:ss";
	private static final String simpleTimeFormat = "HH:mm";

	/**
	 * 获取格式化时间
	 */
	public static String formatDateTime(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(dataTimeFormat);
		return dateFormat.format(date);
	}
	
	/**
	 * 获取格式化时间
	 */
	public static String formatSimpleTime(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(simpleTimeFormat);
		return dateFormat.format(date);
	}
}