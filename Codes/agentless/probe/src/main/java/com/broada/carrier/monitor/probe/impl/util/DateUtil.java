package com.broada.carrier.monitor.probe.impl.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateUtil {
	private static final Log LOG = LogFactory.getLog(DateUtil.class);
	
	public static final String DEFAULT_DATETIME_PATTERN = "MM/dd/yyyy HH:mm:ss";

	/**
	 * 格式化
	 * @param dateStr
	 * @param pattern
	 * @return
	 */
	public static Date format(String dateStr, String pattern){
		Date date = null;
		if(pattern != null)
			try {
				date = new SimpleDateFormat(DEFAULT_DATETIME_PATTERN).parse(dateStr);
			} catch (ParseException e) {
				LOG.error("字符串转换成日期发生异常...");
			}
		return date;
	}
}
