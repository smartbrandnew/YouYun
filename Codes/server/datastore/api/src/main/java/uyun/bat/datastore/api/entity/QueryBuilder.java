package uyun.bat.datastore.api.entity;

import java.io.Serializable;

import uyun.bat.datastore.api.util.PreConditions;

public class QueryBuilder implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long startAbsolute;

	private Long endAbsolute;

	private RelativeTime startRelative;

	private RelativeTime endRelative;

	private QueryMetric metric;

	public static QueryBuilder getInstance() {
		return new QueryBuilder();
	}

	public Long getStartAbsolute() {
		return startAbsolute;
	}

	public QueryBuilder setStartAbsolute(Long startAbsolute) {
		this.startAbsolute = startAbsolute;
		return this;
	}

	public Long getEndAbsolute() {
		return endAbsolute;
	}

	public QueryBuilder setEndAbsolute(Long endAbsolute) {
		this.endAbsolute = endAbsolute;
		return this;
	}

	public RelativeTime getStartRelative() {
		return startRelative;
	}

	public RelativeTime getEndRelative() {
		return endRelative;
	}

	public QueryMetric getMetric() {
		return metric;
	}

	public QueryMetric addMetric(String name) {
		this.metric = new QueryMetric(name);
		return metric;
	}

	public QueryMetric addMetric(QueryMetric metric) {
		this.metric = metric;
		return this.metric;
	}

	public QueryBuilder setStart(int duration, TimeUnit unit)
	{
		PreConditions.checkArgument(duration > 0);
		PreConditions.checkNotNull(unit);
		PreConditions.checkArgument(startAbsolute == null, "Both relative and absolute start times cannot be set.");

		startRelative = new RelativeTime(duration, unit);
		PreConditions.checkArgument(
				startRelative.getTimeRelativeTo(System.currentTimeMillis()) <= System.currentTimeMillis(),
				"Start time cannot be in the future.");
		return this;
	}

	public QueryBuilder setEnd(int duration, TimeUnit unit)
	{
		PreConditions.checkNotNull(unit);
		PreConditions.checkArgument(duration > 0);
		PreConditions.checkArgument(endAbsolute == null, "Both relative and absolute end times cannot be set.");
		endRelative = new RelativeTime(duration, unit);
		return this;
	}

}
