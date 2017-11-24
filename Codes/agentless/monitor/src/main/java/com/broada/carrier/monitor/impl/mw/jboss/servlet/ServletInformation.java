package com.broada.carrier.monitor.impl.mw.jboss.servlet;

public class ServletInformation {

	private String name;

	private long maxTime;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private long processingTime;

	private int requestCount;

	private int errorCount;

	public int getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public long getMaxTime() {
		return maxTime;
	}

	public void setMaxTime(long maxTime) {
		this.maxTime = maxTime;
	}

	public long getProcessingTime() {
		return processingTime;
	}

	public void setProcessingTime(long processingTime) {
		this.processingTime = processingTime;
	}

	public int getRequestCount() {
		return requestCount;
	}

	public void setRequestCount(int requestCount) {
		this.requestCount = requestCount;
	}

	@Override
	public String toString() {
		return "ServletInformation [name=" + name + ", maxTime=" + maxTime + ", processingTime=" + processingTime
				+ ", requestCount=" + requestCount + ", errorCount=" + errorCount + "]";
	}

}
