package com.broada.carrier.monitor.impl.mw.jboss.jvm;

public class PmiJboss7Information {
	private int maxHeapMemory;
	private int usedHeapMemory;
	private int committedHeapMemory;
	private int initHeapMemory;
	private int maxNonHeapMeomory;
	private int usedNonHeapMemory;
	private int committedNonHeapMemory;
	private int initNonHeapMemory;
	private int threadCount;
	private int daemonThreadCount;

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public int getDaemonThreadCount() {
		return daemonThreadCount;
	}

	public void setDaemonThreadCount(int daemonThreadCount) {
		this.daemonThreadCount = daemonThreadCount;
	}

	public int getMaxHeapMemory() {
		return maxHeapMemory;
	}

	public void setMaxHeapMemory(int maxHeapMemory) {
		this.maxHeapMemory = maxHeapMemory;
	}

	public int getUsedHeapMemory() {
		return usedHeapMemory;
	}

	public void setUsedHeapMemory(int usedHeapMemory) {
		this.usedHeapMemory = usedHeapMemory;
	}

	public int getCommittedHeapMemory() {
		return committedHeapMemory;
	}

	public void setCommittedHeapMemory(int committedHeapMemory) {
		this.committedHeapMemory = committedHeapMemory;
	}

	public int getInitHeapMemory() {
		return initHeapMemory;
	}

	public void setInitHeapMemory(int initHeapMemory) {
		this.initHeapMemory = initHeapMemory;
	}

	public int getMaxNonHeapMeomory() {
		return maxNonHeapMeomory;
	}

	public void setMaxNonHeapMeomory(int maxNonHeapMeomory) {
		this.maxNonHeapMeomory = maxNonHeapMeomory;
	}

	public int getUsedNonHeapMemory() {
		return usedNonHeapMemory;
	}

	public void setUsedNonHeapMemory(int usedNonHeapMemory) {
		this.usedNonHeapMemory = usedNonHeapMemory;
	}

	public int getCommittedNonHeapMemory() {
		return committedNonHeapMemory;
	}

	public void setCommittedNonHeapMemory(int committedNonHeapMemory) {
		this.committedNonHeapMemory = committedNonHeapMemory;
	}

	public int getInitNonHeapMemory() {
		return initNonHeapMemory;
	}

	public void setInitNonHeapMemory(int initNonHeapMemory) {
		this.initNonHeapMemory = initNonHeapMemory;
	}

	@Override
	public String toString() {
		return "PmiJboss7Information [maxHeapMemory=" + maxHeapMemory + ", usedHeapMemory=" + usedHeapMemory
				+ ", committedHeapMemory=" + committedHeapMemory + ", initHeapMemory=" + initHeapMemory
				+ ", maxNonHeapMeomory=" + maxNonHeapMeomory + ", usedNonHeapMemory=" + usedNonHeapMemory
				+ ", committedNonHeapMemory=" + committedNonHeapMemory + ", initNonHeapMemory=" + initNonHeapMemory
				+ ", threadCount=" + threadCount + ", daemonThreadCount=" + daemonThreadCount + "]";
	}

}
