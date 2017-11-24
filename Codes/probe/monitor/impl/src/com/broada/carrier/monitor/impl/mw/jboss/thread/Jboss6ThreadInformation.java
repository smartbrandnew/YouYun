package com.broada.carrier.monitor.impl.mw.jboss.thread;

public class Jboss6ThreadInformation {
	private int coreThreads;
	private int maxThreads;
	private int rejectedCount;
	private int currentThreadCount;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCoreThreads() {
		return coreThreads;
	}

	public void setCoreThreads(int coreThreads) {
		this.coreThreads = coreThreads;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public int getRejectedCount() {
		return rejectedCount;
	}

	public void setRejectedCount(int rejectedCount) {
		this.rejectedCount = rejectedCount;
	}

	public int getCurrentThreadCount() {
		return currentThreadCount;
	}

	public void setCurrentThreadCount(int currentThreadCount) {
		this.currentThreadCount = currentThreadCount;
	}

	@Override
	public String toString() {
		return "Jboss6ThreadInformation [coreThreads=" + coreThreads + ", maxThreads=" + maxThreads + ", rejectedCount="
				+ rejectedCount + ", currentThreadCount=" + currentThreadCount + ", name=" + name + "]";
	}

}
