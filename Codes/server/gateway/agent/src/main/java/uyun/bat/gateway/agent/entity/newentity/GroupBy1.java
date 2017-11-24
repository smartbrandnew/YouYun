package uyun.bat.gateway.agent.entity.newentity;

public class GroupBy1 {
	private String tag_key;
	private String aggregator;

	public String getTag_key() {
		return tag_key;
	}

	public void setTag_key(String tag_key) {
		this.tag_key = tag_key;
	}

	public String getAggregator() {
		return aggregator;
	}

	public void setAggregator(String aggregator) {
		this.aggregator = aggregator;
	}

	public GroupBy1() {
	}

	public GroupBy1(String tag_key, String aggregator) {
		this.tag_key = tag_key;
		this.aggregator = aggregator;
	}
}