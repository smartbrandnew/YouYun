package com.broada.carrier.monitor.probe.impl.entity;

public class RemoteMapper {
	private String remoteName;
	private MetricType metricType;
	
	public RemoteMapper(String remoteName, MetricType metricType) {
		this.remoteName = remoteName;
		this.metricType = metricType;
	}
	
	public RemoteMapper() {
	}

	public String getRemoteName() {
		return remoteName;
	}
	public void setRemoteName(String remoteName) {
		this.remoteName = remoteName;
	}
	public MetricType getMetricType() {
		return metricType;
	}
	public void setMetricType(MetricType metricType) {
		this.metricType = metricType;
	}

	@Override
	public String toString() {
		return "RemoteMapper [remoteName=" + remoteName + ", metricType="
				+ metricType + "]";
	}
	
	
	
}
