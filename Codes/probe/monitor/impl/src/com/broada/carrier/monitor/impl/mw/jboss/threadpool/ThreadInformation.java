package com.broada.carrier.monitor.impl.mw.jboss.threadpool;

public class ThreadInformation {
	private int queueSize;
	private int maximumQueueSize;
	private int minimumPoolSize;
	private int maximumPoolSize;
	private int poolNumber;
	private String name;
	private int activeThreads;

	public int getActiveThreads() {
		return activeThreads;
	}

	public void setActiveThreads(int activeThreads) {
		this.activeThreads = activeThreads;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}

	public int getMaximumQueueSize() {
		return maximumQueueSize;
	}

	public void setMaximumQueueSize(int maximumQueueSize) {
		this.maximumQueueSize = maximumQueueSize;
	}

	public int getMinimumPoolSize() {
		return minimumPoolSize;
	}

	public void setMinimumPoolSize(int minimumPoolSize) {
		this.minimumPoolSize = minimumPoolSize;
	}

	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public void setMaximumPoolSize(int maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	public int getPoolNumber() {
		return poolNumber;
	}

	public void setPoolNumber(int poolNumber) {
		this.poolNumber = poolNumber;
	}

	@Override
	public String toString() {
		return "ThreadInformation [queueSize=" + queueSize + ", maximumQueueSize=" + maximumQueueSize
				+ ", minimumPoolSize=" + minimumPoolSize + ", maximumPoolSize=" + maximumPoolSize + ", poolNumber="
				+ poolNumber + ", name=" + name + ", activeThreads=" + activeThreads + "]";
	}

}
