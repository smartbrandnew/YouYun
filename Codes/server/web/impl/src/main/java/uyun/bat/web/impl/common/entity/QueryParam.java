package uyun.bat.web.impl.common.entity;

import java.util.List;

/**
 * 时间序列查询参数
 */
public class QueryParam {
	private String metric;
	private List<Tag> scope;
	private String aggregator;
	private String groupBy;
	private String exclude;

	public String getExclude() {
		return exclude; 
	}

	public void setExclude(String exclude) {
		this.exclude = exclude;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public List<Tag> getScope() {
		return scope;
	}

	public void setScope(List<Tag> scope) {
		this.scope = scope;
	}

	public String getAggregator() {
		return aggregator;
	}

	public void setAggregator(String aggregator) {
		this.aggregator = aggregator;
	}

	public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}
}
