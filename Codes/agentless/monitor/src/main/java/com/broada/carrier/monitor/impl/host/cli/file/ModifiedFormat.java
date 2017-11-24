package com.broada.carrier.monitor.impl.host.cli.file;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.broada.component.utils.text.DateUtil;

/**
 * 用于表示一种文件格式
 * @author Jiangjw
 */
public class ModifiedFormat {
	/**
	 * 注意往数组增加元素时，应遵循越精确越靠前的原则，避免没有正确解析
	 */
	public static final ModifiedFormat[] FORMATS = new ModifiedFormat[] {
		new ModifiedFormat("MMM dd HH:mm"),
		new ModifiedFormat("MM-dd HH:mm"),
		new ModifiedFormat("MMM dd yyyy"),
		new ModifiedFormat("yyyy-MM-dd HH:mm"),
		new ModifiedFormat("yyyy-MM-dd"),
		new ModifiedFormat("yyyy MM dd")
	};
	private SimpleDateFormat format;
	private int fieldCount;
	private boolean needYear;
	private boolean haveTime;

	public ModifiedFormat(String pattern) {
		fieldCount = pattern.split("\\s+").length;
		format = new SimpleDateFormat(pattern, Locale.ENGLISH);
		needYear = !pattern.contains("yy");
		haveTime = pattern.contains("mm");
	}

	/**
	 * 获取本格式化需要的字段数目
	 * @return
	 */
	public int getFieldCount() {
		return fieldCount;
	}

	/**
	 * 使用指定的格式进行格式化
	 * @param text
	 * @param now
	 * @return 成功返回字符串，失败返回null
	 */
	public Date format(String text, Date now) {
		try {
			Date date;
			synchronized (format) {
				date = format.parse(text);
			}			
			if (needYear) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(now);
				int year = calendar.get(Calendar.YEAR);				
				
				calendar.setTime(date);
				calendar.set(Calendar.YEAR, year);
				date = calendar.getTime();
				
				if (date.after(now)) {
					calendar.set(Calendar.YEAR, year - 1);
					date = calendar.getTime();
				}
			}
			if (haveTime)
				return DateUtil.parse(DateUtil.format(date, DateUtil.PATTERN_YYYYMMDD_HHMMSS), DateUtil.PATTERN_YYYYMMDD_HHMMSS);
			else
				return DateUtil.parse(DateUtil.format(date, DateUtil.PATTERN_YYYYMMDD), DateUtil.PATTERN_YYYYMMDD);
		} catch (Throwable e) {
			return null;
		}
	}
}