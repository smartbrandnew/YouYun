package uyun.bat.gateway.dd_agent.entity;

import java.util.List;

public class DDMetric {
	/**
	 * tags
	 */
	private List<TagEntry> tags;
	/**
	 * 指标名
	 */
	private String metric;
	/**
	 * 时间戳
	 */
	private long timestamp;
	/**
	 * 指标值
	 */
	private double value;
	/**
	 * 主机名
	 */
	private String hostName;
	/**
	 * 设备名称
	 */
	private String deviceName;
	/**
	 * 类型
	 */
	private String type;

	public List<TagEntry> getTags() {
		return tags;
	}

	public void setTags(List<TagEntry> tags) {
		this.tags = tags;
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

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
