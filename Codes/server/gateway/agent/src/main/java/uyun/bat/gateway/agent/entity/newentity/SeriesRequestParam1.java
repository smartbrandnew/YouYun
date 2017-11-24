package uyun.bat.gateway.agent.entity.newentity;

import java.util.List;

public class SeriesRequestParam1 {
	private String metric;
	private List<String> tags;
	private long from;
	private long to;
	private String aggregator;
	private int interval;
	private GroupBy1 group_by;

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public long getFrom() {
		return from;
	}

	public void setFrom(long from) {
		this.from = from;
	}

	public long getTo() {
		return to;
	}

	public void setTo(long to) {
		this.to = to;
	}

	public String getAggregator() {
		return aggregator;
	}

	public void setAggregator(String aggregator) {
		this.aggregator = aggregator;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public GroupBy1 getGroup_by() {
		return group_by;
	}

	public void setGroup_by(GroupBy1 group_by) {
		this.group_by = group_by;
	}

	public SeriesRequestParam1() {
	}

	public SeriesRequestParam1(String metric, List<String> tags, long from, long to, String aggregator, int interval,
			GroupBy1 group_by) {
		this.metric = metric;
		this.tags = tags;
		this.from = from;
		this.to = to;
		this.aggregator = aggregator;
		this.interval = interval;
		this.group_by = group_by;
	}
	
}
