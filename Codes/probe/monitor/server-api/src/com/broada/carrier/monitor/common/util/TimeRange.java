package com.broada.carrier.monitor.common.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.broada.component.utils.text.DateUtil;

/**
 * 时间区间
 * @author Jiangjw
 */
public class TimeRange {
	private long start;
	private long end;

	public TimeRange(long start, long end) {
		set(start, end);
	}

	public TimeRange(Date start, Date end) {
		set(start == null ? 0 : start.getTime(), end == null ? 0 : end.getTime());
	}
	
	protected void set(long start, long end) {
		if (start > end)
			throw new IllegalArgumentException("起始时间不能晚于结束时间");
		this.start = start;
		this.end = end;
	}

	/**
	 * 计算当前时间区间与start2~end2的包含关系，返回剔除掉start2~end2的时间段
	 * @param start2
	 * @param end2
	 * @return 如果当前时间区间完全被start2~end2包含，则返回null
	 */
	public TimeRange[] remove(long start2, long end2) {
		if (start2 <= start && end2 >= end)
			return null;

		boolean containsStart = contains(start2);
		boolean containsEnd = contains(end2);
		if (containsStart && containsEnd) {
			if (start2 == start) {
				if (end2 >= end)
					return null;
				else
					return new TimeRange[] { new TimeRange(end2, end) };
			} else {
				if (end2 >= end)
					return new TimeRange[] { new TimeRange(start, start2) };
				else
					return new TimeRange[] { new TimeRange(start, start2), new TimeRange(end2, end) };
			}
		} else if (containsStart) {
			if (start2 == start)
				return null;
			else
				return new TimeRange[] { new TimeRange(start, start2) };
		} else if (containsEnd) {
			if (end2 == end)
				return null;
			else
				return new TimeRange[] { new TimeRange(end2, end) };
		} else
			return new TimeRange[] { this };
	}

	/**
	 * 返回当前时间区间是否包含了time时间
	 * 注意，公式是：time >= start && time < end;
	 * @param time 单位ms
	 * @return
	 */
	public boolean contains(long time) {
		return time >= start && time < end;
	}

	/**
	 * 返回起始时间
	 * @return
	 */
	public long getStart() {
		return start;
	}

	/**
	 * 返回结束时间
	 * @return
	 */
	public long getEnd() {
		return end;
	}

	/**
	 * {@link #remove(long, long)}
	 * @param start2
	 * @param end2
	 * @return
	 */
	public TimeRange[] remove(Date start2, Date end2) {
		return remove(start2.getTime(), end2.getTime());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		TimeRange other = (TimeRange) obj;
		return this.start == other.start && this.end == other.end;		
	}

	@Override
	public String toString() {
		return String.format("%s~%s", DateUtil.format(new Date(start)), DateUtil.format(new Date(end)));
	}

	public static TimeRange getToday() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		long start = cal.getTimeInMillis();
		
		cal.add(Calendar.DAY_OF_MONTH, 1);
		long end = cal.getTimeInMillis();
		
		return new TimeRange(start, end);
	}	
}