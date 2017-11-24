package uyun.bat.web.impl.common.entity;

public class TopNQueryParam extends QueryParam {
	private int limit;
	private String order;
	private String aggregatorValue;

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getAggregatorValue() {
		return aggregatorValue;
	}

	public void setAggregatorValue(String aggregatorValue) {
		this.aggregatorValue = aggregatorValue;
	}

}
