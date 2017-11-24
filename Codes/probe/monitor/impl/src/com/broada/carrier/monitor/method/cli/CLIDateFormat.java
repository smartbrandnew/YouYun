package com.broada.carrier.monitor.method.cli;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CLIDateFormat {
	private static final Logger logger = LoggerFactory.getLogger(CLIDateFormat.class);
	public static DatePattern[] DATE_FORMATERS;
  
  static {
  	DATE_FORMATERS = new DatePattern[]{
  			new DatePattern("MMM dd HH:mm yyyy", Locale.ENGLISH),
  			new DatePattern("MMM dd HH:mm", Locale.ENGLISH),
  			new DatePattern("MMM dd yyyy", Locale.ENGLISH),
  			new DatePattern("yyyy-MM-dd HH:mm:ss"),
  			new DatePattern("yyyy-MM-dd"),  			
  			new DatePattern("yyyy/MM/dd HH:mm:ss"),
  			new DatePattern("yyyy/MM/dd E a hh:mm:ss", Locale.CHINESE),
  			new DatePattern("yyyy/MM/dd E HH:mm:ss", Locale.CHINESE),
  			new DatePattern("dd MMM HH:mm yyyy", Locale.ENGLISH),
  			new DatePattern("MM dd HH:mm", Locale.ENGLISH),  			
  			new DatePattern("HH:mm", Locale.ENGLISH)
  	};  	  	
  }
  
  /**
   * 将修改时间格式化成"2007-11-7 11:12:52"的格式; mtime 可能有三种格式: "2007-11-7_11:12:52"、"Dec 28 17:01 2006"、"28 Dec 17:01 2006"
   * 
   * @param mtime
   * @return
   */
  public static Date format(String strMmtime) {
  	strMmtime = strMmtime.replaceAll("_", " ");
  	Date mtime = null;
  	synchronized (DATE_FORMATERS) {
  		DatePattern matchFormatter = null;
			for (DatePattern formatter : DATE_FORMATERS) {
				try {
		      mtime = formatter.parse(strMmtime);
		      matchFormatter = formatter;
		      break;
		    } catch (ParseException e) {
		    	if (logger.isDebugEnabled())
		    		logger.debug(String.format("解析日期格式[str: %s format: %s]失败。错误：", strMmtime, formatter.toPattern()), e);					
		    	continue;
		    }
			}
			
			if (mtime == null) {
	  		throw new IllegalArgumentException("无效格式的文件修改时间:" + strMmtime);
	  	}    
			
			if (matchFormatter != null) {
				if (matchFormatter.needDate) {
					Date now = new Date();
			    Calendar c = Calendar.getInstance();
			    c.setTime(mtime);
			    int hour = c.get(Calendar.HOUR_OF_DAY);
			    int min = c.get(Calendar.MINUTE);
			    int sec = c.get(Calendar.SECOND);
			    c.setTime(now);
			    c.set(Calendar.HOUR_OF_DAY, hour);
			    c.set(Calendar.MINUTE, min);
			    c.set(Calendar.SECOND, sec);
			    mtime = c.getTime();					
				} else if (matchFormatter.needYear) {
					Date now = new Date();
			    Calendar c = Calendar.getInstance();
			    c.setTime(mtime);
			    int hour = c.get(Calendar.HOUR_OF_DAY);
			    int min = c.get(Calendar.MINUTE);
			    int sec = c.get(Calendar.SECOND);
			    int day = c.get(Calendar.DAY_OF_MONTH);
			    int mon = c.get(Calendar.MONTH);
			    c.setTime(now);
			    c.set(Calendar.HOUR_OF_DAY, hour);
			    c.set(Calendar.MINUTE, min);
			    c.set(Calendar.SECOND, sec);
			    c.set(Calendar.DAY_OF_MONTH, day);
			    c.set(Calendar.MONTH, mon);
			    mtime = c.getTime();				
				}
			}
			
	    return mtime;
		}  	
  }
  
  private static class DatePattern {
  	private SimpleDateFormat format;  	
  	private boolean needYear;  	
  	private boolean needDate;
  	
  	public DatePattern(String pattern) {
  		format = new SimpleDateFormat(pattern);
  		needYear = !pattern.contains("yy");
  	}
  	
  	public Object toPattern() {
			return format.toPattern();
		}

		public Date parse(String text) throws ParseException {
			return format.parse(text);
		}

		public DatePattern(String pattern, Locale locale) {
  		format = new SimpleDateFormat(pattern, locale);
  		needYear = !pattern.contains("yy");
  		needDate = !pattern.contains("dd");
  	}  	
  }
}
