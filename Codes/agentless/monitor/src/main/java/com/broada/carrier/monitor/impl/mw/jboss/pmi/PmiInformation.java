package com.broada.carrier.monitor.impl.mw.jboss.pmi;

public class PmiInformation {
	private long applyMemory;
	private long totalMemory;
	private long freeMemory;

	public long getApplyMemory() {
		return applyMemory;
	}

	public void setApplyMemory(long applyMemory) {
		this.applyMemory = applyMemory;
	}

	public long getTotalMemory() {
		return totalMemory;
	}

	public void setTotalMemory(long totalMemory) {
		this.totalMemory = totalMemory;
	}

	public long getFreeMemory() {
		return freeMemory;
	}

	public void setFreeMemory(long freeMemory) {
		this.freeMemory = freeMemory;
	}

	@Override
	public String toString() {
		return "PmiInformation [applyMemory=" + applyMemory + ", totalMemory=" + totalMemory + ", freeMemory=" + freeMemory
				+ "]";
	}

}
