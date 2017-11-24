package uyun.bat.datastore.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

	private static final String ymdhmsFormat = "yyyy-MM-dd HH:mm:ss";

	private static final String ymdhmFormat = "yyyy-MM-dd HH:mm";

	public static final String ymFormat = "yyyy-MM";

	private static final String ymdFormat = "yyyy-MM-dd";

	private static final String mdhmFormat = "MM-dd HH:mm";

	private static final String hmsFormat = "HH:mm:ss";

	private static final String yMdHmsFormatFormat = "yyyyMMddHHmmss";

	private static final String ymdThmsZFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	private static final String ymdhFormat = "yyyy-MM-dd HH";

	/**
	 *
	 * @Title: str2Day
	 * @Description: 字符串时间转化为date（yyyy-MM-dd）
	 * @param @param strDate
	 * @param @return
	 * @return Date
	 */
	public static Date str2Day(String strDate) {
		if (strDate == null)
			return null;
		Date ret;
		try {
			DateFormat dateFormat = new SimpleDateFormat(ymdFormat);
			ret = dateFormat.parse(strDate);
		} catch (ParseException e) {
			throw new RuntimeException("Date parse error!");
		}
		return ret;
	}

	/**
	 *
	 * @Title: toDate
	 * @Description: 字符串时间转化为date（yyyy-MM-dd HH:mm:ss）
	 * @param @param strDate
	 * @param @return
	 * @return Date
	 */
	public static Date str2Time(String strDate) {
		if (strDate == null)
			return null;
		Date ret;
		try {
			DateFormat dateFormat = new SimpleDateFormat(ymdhmsFormat);
			ret = dateFormat.parse(strDate);
		} catch (ParseException e) {
			return null;
		}
		return ret;
	}

	public static Date str2TimeHm(String strDate) {
		if (strDate == null)
			return null;
		Date ret;
		try {
			DateFormat dateFormat = new SimpleDateFormat(ymdhmFormat);
			ret = dateFormat.parse(strDate);
		} catch (ParseException e) {
			throw new RuntimeException("Date parse error!");
		}
		return ret;
	}

	public static Date str2TimeH(String strDate) {
		if (strDate == null)
			return null;
		Date ret;
		try {
			DateFormat dateFormat = new SimpleDateFormat(ymdhFormat);
			ret = dateFormat.parse(strDate);
		} catch (ParseException e) {
			throw new RuntimeException("Date parse error!");
		}
		return ret;
	}

	public static Date fmtUTC2Date(String str) {
		Date date;
		try {
			SimpleDateFormat format = new SimpleDateFormat(ymdThmsZFormat);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			date = format.parse(str);
		} catch (ParseException e) {
			throw new RuntimeException("Date parse error!");
		}
		return date;
	}

	/**
	 *
	 * @Title: fmt2TimeStr
	 * @Description: 把日期格式转换成格式(yyyy-MM-dd HH:mm:ss)
	 * @param @param date
	 * @param @return
	 * @return String
	 */
	public static String fmt2TimeStr(Date date) {
		if (date == null)
			return null;
		DateFormat dateFormat = new SimpleDateFormat(ymdhmsFormat);
		return dateFormat.format(date);
	}

	/**
	 *
	 * @Title: getNumFmtDate
	 * @Description: 获得当前时间，格式如：20140128150501
	 * @param @param date
	 * @param @return
	 * @return String
	 */
	public static String getNumFmtDate(Date date) {
		String str = fmt2TimeStr(date);
		str = str.replace("-", "");
		str = str.replace("-", "");
		str = str.replace("-", "");
		str = str.replace(":", "");
		str = str.replace(":", "");
		str = str.replace(":", "");
		str = str.replace(" ", "");
		return str;
	}

	/**
	 *
	 * @Title: getNumStr2Time
	 * @Description: 字符串"20140128150501" 转换为 date "2014-01-28 15:05:01"
	 * @param @param str
	 * @param @return
	 * @param @throws ParseException
	 * @return Date
	 */
	public static Date getNumStr2Time(String str) {
		if (str == null || str.length() < 14) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(str.substring(0, 4)).append("-");
		sb.append(str.substring(4, 6)).append("-");
		sb.append(str.substring(6, 8)).append(" ");
		sb.append(str.substring(8, 10)).append(":");
		sb.append(str.substring(10, 12)).append(":");
		sb.append(str.substring(12, 14));
		return str2Time(sb.toString());
	}

	public static Date getDateAdd(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return new Date(cal.getTime().getTime());
	}

}
