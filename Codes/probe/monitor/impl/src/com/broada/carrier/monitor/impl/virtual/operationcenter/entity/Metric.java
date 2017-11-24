package com.broada.carrier.monitor.impl.virtual.operationcenter.entity;

import java.util.Date;
import java.util.Map;

public class Metric {
	Map<String, String> metrics;
	Date time;
	
	public Map<String, String> getMetrics() {
		return metrics;
	}
	public void setMetrics(Map<String, String> metrics) {
		this.metrics = metrics;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
}
