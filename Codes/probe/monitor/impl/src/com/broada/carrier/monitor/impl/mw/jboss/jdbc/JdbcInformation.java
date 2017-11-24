package com.broada.carrier.monitor.impl.mw.jboss.jdbc;

public class JdbcInformation {
	private String name;

	private long availableConnCount;

	private long inUseConnCount;

	private int connCreatedCount;

	private int connDestroyedCount;

	private long maxConnInUseCount;

	private int connCount;

	private int blockingTimeoutMillis;

	private int maxSize;

	private int minSize;

	private long idleTimeoutMinutes;

	private double usePercent;

	public long getAvailableConnCount() {
		return availableConnCount;
	}

	public void setAvailableConnCount(long availableConnCount) {
		this.availableConnCount = availableConnCount;
	}

	public long getInUseConnCount() {
		return inUseConnCount;
	}

	public void setInUseConnCount(long inUseConnCount) {
		this.inUseConnCount = inUseConnCount;
	}

	public int getConnCreatedCount() {
		return connCreatedCount;
	}

	public void setConnCreatedCount(int connCreatedCount) {
		this.connCreatedCount = connCreatedCount;
	}

	public int getConnDestroyedCount() {
		return connDestroyedCount;
	}

	public void setConnDestroyedCount(int connDestroyedCount) {
		this.connDestroyedCount = connDestroyedCount;
	}

	public long getMaxConnInUseCount() {
		return maxConnInUseCount;
	}

	public void setMaxConnInUseCount(long maxConnInUseCount) {
		this.maxConnInUseCount = maxConnInUseCount;
	}

	public int getConnCount() {
		return connCount;
	}

	public void setConnCount(int connCount) {
		this.connCount = connCount;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public int getMinSize() {
		return minSize;
	}

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public int getBlockingTimeoutMillis() {
		return blockingTimeoutMillis;
	}

	public void setBlockingTimeoutMillis(int blockingTimeoutMillis) {
		this.blockingTimeoutMillis = blockingTimeoutMillis;
	}

	public long getIdleTimeoutMinutes() {
		return idleTimeoutMinutes;
	}

	public void setIdleTimeoutMinutes(long idleTimeoutMinutes) {
		this.idleTimeoutMinutes = idleTimeoutMinutes;
	}

	public double getUsePercent() {
		return usePercent;
	}

	@Override
	public String toString() {
		return "JdbcInformation [availableConnCount=" + availableConnCount + ", inUseConnCount=" + inUseConnCount
				+ ", connCreatedCount=" + connCreatedCount + ", connDestroyedCount=" + connDestroyedCount
				+ ", maxConnInUseCount=" + maxConnInUseCount + ", connCount=" + connCount + ", blockingTimeoutMillis="
				+ blockingTimeoutMillis + ", maxSize=" + maxSize + ", minSize=" + minSize + ", idleTimeoutMinutes="
				+ idleTimeoutMinutes + ", usePercent=" + usePercent + "]";
	}

	public void setUsePercent(double usePercent) {
		this.usePercent = usePercent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
