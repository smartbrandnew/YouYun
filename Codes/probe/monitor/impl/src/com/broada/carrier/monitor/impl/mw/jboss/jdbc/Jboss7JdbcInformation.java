package com.broada.carrier.monitor.impl.mw.jboss.jdbc;

public class Jboss7JdbcInformation {
	private int blockingTimeoutMillis;

	private int maxSize;

	private int minSize;

	private long idleTimeoutMinutes;

	private long newConnectionSql;

	private int poolSize;

	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	public int getBlockingTimeoutMillis() {
		return blockingTimeoutMillis;
	}

	public void setBlockingTimeoutMillis(int blockingTimeoutMillis) {
		this.blockingTimeoutMillis = blockingTimeoutMillis;
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

	public long getIdleTimeoutMinutes() {
		return idleTimeoutMinutes;
	}

	public void setIdleTimeoutMinutes(long idleTimeoutMinutes) {
		this.idleTimeoutMinutes = idleTimeoutMinutes;
	}

	public long getNewConnectionSql() {
		return newConnectionSql;
	}

	public void setNewConnectionSql(long newConnectionSql) {
		this.newConnectionSql = newConnectionSql;
	}

	@Override
	public String toString() {
		return "Jboss7JdbcInformation [blockingTimeoutMillis=" + blockingTimeoutMillis + ", maxSize=" + maxSize
				+ ", minSize=" + minSize + ", idleTimeoutMinutes=" + idleTimeoutMinutes + ", newConnectionSql="
				+ newConnectionSql + ", poolSize=" + poolSize + "]";
	}

}
