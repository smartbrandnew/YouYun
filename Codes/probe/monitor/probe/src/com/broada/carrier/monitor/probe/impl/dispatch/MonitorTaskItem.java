package com.broada.carrier.monitor.probe.impl.dispatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.broada.carrier.monitor.common.util.TimeRange;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.component.utils.text.DateUtil;

/**
 * 监测服务包装器，中用记录调度信息
 * @author Jiangjw
 */
class MonitorTaskItem extends BaseItem implements Comparable<MonitorTaskItem> {	
	private static final long DAY_MS = 60l * 60 * 24 * 1000;
	private MonitorNode node;
	private MonitorTask task;
	private MonitorRecord record;
	private MonitorPolicy policy;
	
	public MonitorNode getNode() {
		return node;
	}

	public void setNode(MonitorNode node) {
		this.node = node;
	}

	private long nextRunTime;		

	public MonitorTaskItem(MonitorTask task, MonitorPolicy policy, MonitorRecord record) {
		this(task, policy, record, MonitorResultUploader.getDefault().getServerTime());
	}

	public MonitorTaskItem(MonitorTask task, MonitorPolicy policy, MonitorRecord record, Date now) {
		this.task = task;
		this.record = record;
		this.policy = policy;
		if (record != null || policy != null)
			this.nextRunTime = calNextRunTime(now.getTime());
	}

	public MonitorTask getTask() {
		return task;
	}

	public MonitorRecord getRecord() {
		return record;
	}

	public MonitorPolicy getPolicy() {
		return policy;
	}

	/**
	 * 计划运行时间
	 * @return
	 */
	public long getNextRunTime() {
		return nextRunTime;
	}

	@Override
	public int hashCode() {
		return record.getTaskId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		MonitorTaskItem other = (MonitorTaskItem) obj;
		return this.task.getId() == other.task.getId();
	}

	/**
	 * 计算下次运行时间
	 * 
	 * @param srv
	 * @return
	 */
	private long calNextRunTime(long now) {
		if (isAllStop())
			return Long.MAX_VALUE;

		long interval;
		if (record.getState().isError())
			interval = policy.getErrorInterval() * 1000l;
		else
			interval = policy.getInterval() * 1000l;

		long lastRunTime = record.getTime().getTime();
		long nextRunTime = lastRunTime + interval;
		if (nextRunTime < now)
			nextRunTime = now;
		for (long nextDayTime = nextRunTime;; nextDayTime += DAY_MS) {
			TimeRange range = calDayMonTime(nextDayTime);
			if (range == null)
				continue;

			TimeRange[] ranges = removeStopTime(range);
			if (ranges == null || ranges.length == 0)
				continue;

			for (TimeRange r : ranges) {
				if (r.contains(nextRunTime))
					return nextRunTime;
				else if (r.getStart() - record.getTime().getTime() > interval && r.getStart() > now)
					return r.getStart();
			}
		}
	}

	private boolean isAllStop() {
		return !task.isEnabled()
				|| (policy.retWorkTimeRange().getStart() != 0 && policy.retWorkTimeRange().getStart() == policy
						.retWorkTimeRange().getEnd())
				|| (policy.getWorkWeekDays() == null || policy.getWorkWeekDays().length() == 0);
	}

	private TimeRange[] removeStopTime(TimeRange range) {
		if (policy.retStopTimeRanges().length == 0)
			return new TimeRange[] { range };

		List<TimeRange> result = new ArrayList<TimeRange>();
		for (TimeRange time : policy.retStopTimeRanges()) {
			TimeRange[] ranges = range.remove(time.getStart(), time.getEnd());
			if (ranges == null)
				return null;

			if (ranges.length == 2)
				result.add(ranges[0]);
			range = ranges[ranges.length - 1];
		}
		result.add(range);
		return result.toArray(new TimeRange[result.size()]);
	}

	private TimeRange calDayMonTime(long now) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(now);
		if (!isInWeekDay(cal))
			return null;

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long dayZero = cal.getTimeInMillis();
		long start = dayZero + policy.retWorkTimeRange().getStart() - MonitorPolicy.WORK_TIME_MIN.getTime();
		long end = dayZero + policy.retWorkTimeRange().getEnd() - MonitorPolicy.WORK_TIME_MIN.getTime() + 1000;
		return new TimeRange(start, end);
	}

	private boolean isInWeekDay(Calendar nowCal) {
		String dayOfWeek = policy.getWorkWeekDays();
		int week = nowCal.get(Calendar.DAY_OF_WEEK) - 1;
		return dayOfWeek.contains(Integer.toString(week));
	}

	@Override
	public int compareTo(MonitorTaskItem o) {
		long n1 = this.nextRunTime;
		long n2 = o.nextRunTime;
		if (n1 > n2)
			return 1;
		else if (n1 == n2 && this.equals(o))
			return 0;
		else
			return -1;
	}

	@Override
	public String toString() {
		return String.format("%s[%s nextRun: %s time: %d]", getClass().getSimpleName(), task,
				DateUtil.format(new Date(nextRunTime)), getTime());
	}
}