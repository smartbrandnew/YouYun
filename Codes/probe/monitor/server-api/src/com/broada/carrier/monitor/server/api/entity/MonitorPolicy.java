package com.broada.carrier.monitor.server.api.entity;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.broada.carrier.monitor.common.util.ObjectUtil;
import com.broada.carrier.monitor.common.util.TextUtil;
import com.broada.carrier.monitor.common.util.TimeRange;
import com.broada.component.utils.text.DateUtil;
import com.broada.component.utils.text.Unit;

public class MonitorPolicy implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_POLICY_CODE = "defaultPolicy";
	public static final String WORK_TIME_FORMAT = "HH:mm:ss";
	public static final String DEFAULT_WORK_WEEK_DAYS = "0123456";	
	public static final Date WORK_TIME_MIN;
	public static final Date WORK_TIME_MAX;		
	
	private String code;
	private String name;
	private String descr;
	private int interval;
	private int errorInterval;
	private String workWeekDays;
	private String workTimeRange;
	private String stopTimeRanges;
	private long modified;
	private TimeRange workTimeRangeCache;
	private TimeRangeInfo[] stopTimeRangesCache;
	
	static {
		try {
			WORK_TIME_MIN = DateUtil.parse("00:00:00", WORK_TIME_FORMAT);
			WORK_TIME_MAX = DateUtil.parse("23:59:59", WORK_TIME_FORMAT);
		} catch (Throwable e) {
			throw new RuntimeException(e);					
		}
	}

	public long getModified() {
		return modified;
	}

	public void setModified(long modified) {
		this.modified = modified;
	}

	public MonitorPolicy() {
		workWeekDays = DEFAULT_WORK_WEEK_DAYS;
		interval = 600;
		errorInterval = 300;
	}

	public MonitorPolicy(String code, String name, int interval, int errorInterval) {
		set(code, name, interval, errorInterval, DEFAULT_WORK_WEEK_DAYS, null, null, null, 0);
	}

	public MonitorPolicy(String code, String name, int interval, int errorInterval, String workWeekDays,
			String workTimeRange,	String stopTimeRanges, String descr, long modified) {
		set(code, name, interval, errorInterval, workWeekDays, workTimeRange, stopTimeRanges, descr, modified);		
	}

	public MonitorPolicy(MonitorPolicy copy) {
		set(copy);		
	}

	public String getName() {
		if (name == null || name.isEmpty())
			return getCode();
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 正常监测周期，单位s
	 * 表示监测任务在状态正常或未监测情况下，在上次监测完成后，再等多少时间启动下一次监测
	 * @return
	 */
	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	/**
	 * 错误监测周期，单位s
	 * 表示监测任务在状态失败情况下，在上次监测完成后，再等多少时间启动下一次监测
	 * @return
	 */
	public int getErrorInterval() {
		return errorInterval;
	}

	public void setErrorInterval(int errorInterval) {
		this.errorInterval = errorInterval;
	}

	public String getWorkWeekDays() {
		return workWeekDays;
	}

	public void setWorkWeekDays(String workWeekDays) {
		this.workWeekDays = workWeekDays;
	}

	public TimeRangeInfo[] retStopTimeRanges() {
		if (stopTimeRangesCache == null) {
			String text = getStopTimeRanges();
			if (text == null || text.isEmpty())
				stopTimeRangesCache = new TimeRangeInfo[0];
			else {
				String[] lines = TextUtil.splitLines(stopTimeRanges);
				List<TimeRangeInfo> list = new ArrayList<TimeRangeInfo>(lines.length);
				for (String line : lines) {
					line = line.trim();
					if (line.isEmpty())
						continue;
					try {
						list.add(TimeRangeInfo.decode(line));
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException("停用时间格式错误，" + e.getMessage(), e);
					}
				}
				stopTimeRangesCache = list.toArray(new TimeRangeInfo[list.size()]);
			}
		}
		return stopTimeRangesCache;
	}
	
	private static String encode(TimeRange timeRange) {
		return DateUtil.format(new Date(timeRange.getStart()), WORK_TIME_FORMAT)
				+ "~"
				+ DateUtil.format(new Date(timeRange.getEnd()), WORK_TIME_FORMAT);
	}
	
	private static TimeRange decode(String text) {		
		if (text == null || text.isEmpty())
			return new TimeRange(WORK_TIME_MIN, WORK_TIME_MAX);
		else {
			String[] fields = text.split("~");
			if (fields.length != 2)
				throw new IllegalArgumentException("工作时间格式错误，必须是使用~分隔的时间字符串：" + text);

			try {
				Date start = DateUtil.parse(fields[0], WORK_TIME_FORMAT);
				Date end = DateUtil.parse(fields[1], WORK_TIME_FORMAT);
				if (start.after(end))
					throw new IllegalArgumentException("工作时间错误，起始时间必须早于结束时间：" + text);

				return new TimeRange(start, end);
			} catch (ParseException e) {
				throw new IllegalArgumentException("工作时间格式错误，必须是标准的时间格式（hh:mm:ss）：" + text);
			}
		}
	}
	
	public TimeRange retWorkTimeRange() {
		if (workTimeRangeCache == null) 
			workTimeRangeCache = decode(getWorkTimeRange());
		return workTimeRangeCache;		
	}

	public String getWorkTimeRange() {
		return workTimeRange;
	}

	public void setWorkTimeRange(String workTimeRange) {
		this.workTimeRange = workTimeRange;
		this.workTimeRangeCache = null;
	}
	
	public void putWorkTimeRange(TimeRange timeRange) {
		setWorkTimeRange(encode(timeRange));
		this.workTimeRangeCache = timeRange;
	}

	public String getStopTimeRanges() {
		return stopTimeRanges;
	}

	public void setStopTimeRanges(String stopTimeRanges) {
		this.stopTimeRanges = stopTimeRanges;
		this.stopTimeRangesCache = null;
	}
	
	public void putStopTimeRanges(TimeRangeInfo[] stopTimeRanges) {		
		StringBuilder sb = new StringBuilder();
		for (TimeRangeInfo time : stopTimeRanges) {
			if (sb.length() > 0)
				sb.append('\n');
			sb.append(time.encode());
		}
		setStopTimeRanges(sb.toString());
		this.stopTimeRangesCache = stopTimeRanges;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}
	
	public boolean retWorkWeekDaysContains(int day) {
		if (workWeekDays == null)
			return true;
		return workWeekDays.contains(Integer.toString(day));
	}

	@Override
	public String toString() {
		return String.format("%s[code: %s name: %s interval: %d errorInterval: %d]", getClass().getSimpleName(), getCode(), getName(), getInterval(), getErrorInterval());
	}

	@Override
	public boolean equals(Object obj) {
		MonitorPolicy other = (MonitorPolicy) obj;
		return this.getCode().equals(other.getCode()) && this.getModified() == other.getModified();
	}

	@Override
	public int hashCode() {
		return getCode().hashCode();
	}
	
	public void set(String code, String name, int interval, int errorInterval, String workWeekDays,
			String workTimeRange,	String stopTimeRanges, String descr, long modified) {
		this.code = code;
		this.name = name;
		this.interval = interval;
		this.errorInterval = errorInterval;
		this.workWeekDays = workWeekDays;
		setWorkTimeRange(workTimeRange);
		setStopTimeRanges(stopTimeRanges);
		this.modified = modified;
		this.descr = descr;
	}

	public void set(MonitorPolicy copy) {
		set(copy.getCode(), copy.getName(), copy.getInterval(), copy.getErrorInterval(), copy.getWorkWeekDays(),
				copy.getWorkTimeRange(), copy.getStopTimeRanges(), copy.getDescr(), copy.getModified());
	}

	public void verify() {
		if (!TextUtil.isLegalCode(getCode()))
			throw new IllegalArgumentException("编码不允许为空，并且只能由字符、数字、减号与下划线组成");
	}

	public String retDisplayName() {
		return String.format("%s[%s/%s]", getName(), Unit.second.formatPrefer(getInterval()), Unit.second.formatPrefer(getErrorInterval()));
	}

	public boolean retDefault() {
		return DEFAULT_POLICY_CODE.equals(getCode());
	}

	public boolean equalsData(MonitorPolicy item) {
		return this.interval == item.interval
				&& this.errorInterval == item.errorInterval
				&& ObjectUtil.equals(this.workWeekDays, item.workWeekDays)
				&& ObjectUtil.equals(this.workTimeRange, item.workTimeRange)
				&& ObjectUtil.equals(this.stopTimeRanges, item.stopTimeRanges);
	}
}
