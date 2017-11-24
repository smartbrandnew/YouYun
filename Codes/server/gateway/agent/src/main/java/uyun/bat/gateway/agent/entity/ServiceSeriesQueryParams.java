package uyun.bat.gateway.agent.entity;

import java.util.List;

import uyun.bat.datastore.api.entity.AggregatorType;
import uyun.bat.gateway.agent.entity.newentity.GroupBy1;

public class ServiceSeriesQueryParams {
	private String userId;
	private String tenantId;
	private String metric;
	private List<String> tags;
	private long from;
	private long to;
	private AggregatorType aggregator;
	private int interval;
	private GroupBy1 group_by;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
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
	public AggregatorType getAggregator() {
		return aggregator;
	}
	public void setAggregator(AggregatorType aggregator) {
		this.aggregator = aggregator;
	}
	@Override
	public String toString() {
		return "ServiceSeriesQueryParams [userId=" + userId + ", tenantId=" + tenantId + ", metric=" + metric
				+ ", tags=" + tags + ", from=" + from + ", to=" + to + ", aggregator=" + aggregator + ", interval="
				+ interval + ", group_by=" + group_by + "]";
	}
	
	
	
}
