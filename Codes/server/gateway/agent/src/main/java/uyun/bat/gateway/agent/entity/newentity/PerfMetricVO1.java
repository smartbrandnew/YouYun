package uyun.bat.gateway.agent.entity.newentity;

import java.util.List;

public class PerfMetricVO1 {
	private String host;
	private String host_id;
	private String metric;
	private long timestamp = System.currentTimeMillis();
	private double value;
	private List<String> tags;
	private String type = "gauge";

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHost_id() {
		return host_id;
	}

	public void setHost_id(String host_id) {
		this.host_id = host_id;
	}

	public PerfMetricVO1() {
	}

	public PerfMetricVO1(String host, String host_id, String metric, long timestamp, double value, List<String> tags,
			String type) {
		this.host = host;
		this.host_id = host_id;
		this.metric = metric;
		this.timestamp = timestamp;
		this.value = value;
		this.tags = tags;
		this.type = type;
	}
}
