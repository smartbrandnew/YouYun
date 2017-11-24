package com.broada.carrier.monitor.server.api.entity;

import java.text.ParseException;
import java.util.Date;

import com.broada.carrier.monitor.common.util.TimeRange;
import com.broada.component.utils.text.DateUtil;

public class TimeRangeInfo extends TimeRange {
	private String descr;

	public TimeRangeInfo() {	
		this(null, null, null);
	}
	
	public TimeRangeInfo(Date start, Date end, String descr) {
		super(start, end);
		this.descr = descr;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}
	
	public static TimeRangeInfo decode(String text) {
		int pos = text.indexOf("~");
		if (pos < 0)
			throw new IllegalArgumentException("必须是使用~分隔的时间字符串：" + text);
		
		String field0 = text.substring(0, pos);		
		String field1 = text.substring(pos + 1);
		pos = field1.indexOf(";");
		String field2 = null;
		if (pos >= 0) {
			if (pos < field1.length())
				field2 = field1.substring(pos + 1);
			field1 = field1.substring(0, pos);			
		}
				
		Date start = decodeDate(field0);
		Date end = decodeDate(field1);
		if (start.after(end))
			throw new IllegalArgumentException("起始时间必须早于结束时间：" + text);

		return new TimeRangeInfo(start, end, field2);
	}

	private static Date decodeDate(String text) {
		try {
			return DateUtil.parse(text, DateUtil.PATTERN_YYYYMMDD_HHMMSS);
		} catch (ParseException e) {
			try {
				return DateUtil.parse(text, DateUtil.PATTERN_YYYYMMDD);
			} catch (ParseException e1) {
				throw new IllegalArgumentException("必须是标准的日期时间格式（yyyy-mm-dd）：" + text);
			}
		}
	}

	public String encode() {
		return String.format("%s~%s;%s",
				DateUtil.format(new Date(getStart())),
				DateUtil.format(new Date(getEnd())),
				getDescr() == null ? "" : getDescr());
	}

	public void set(TimeRangeInfo copy) {
		set(copy.getStart(), copy.getEnd(), copy.getDescr());
	}

	public void set(long start, long end, String descr) {
		set(start, end);
		setDescr(descr);
	}
}
