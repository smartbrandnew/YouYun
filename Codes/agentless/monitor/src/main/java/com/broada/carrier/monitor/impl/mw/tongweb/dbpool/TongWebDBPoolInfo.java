package com.broada.carrier.monitor.impl.mw.tongweb.dbpool;

import com.broada.carrier.monitor.impl.mw.tongweb.TongWeb;

public class TongWebDBPoolInfo implements TongWeb {
	private Integer maxSize;

	private Integer minSize;

	private Integer connectionCount;

	private Long inUseConnectionCount;

	private Integer connectionCreatedCount;

	private Integer connectionDestroyedCount;

	private Long availableConnectionCount;

	private Long maxConnectionsInUseCount;

	private Integer blockingTimeoutMillis;

	private Long idleTimeoutMinutes;

	private Long connectionTimeoutMinutes;

	private Integer state;

	private String stateString;

	/**
	 * "ManagedConnectionPool".
	 */
	private String service;

	/**
	 * 当前DB连接池的名称.
	 */
	private String name;

	/**
	 * "tongweb.jca".
	 */
	private String domain;

	public Long getAvailableConnectionCount() {
		return availableConnectionCount;
	}

	public void setAvailableConnectionCount(Long availableConnectionCount) {
		this.availableConnectionCount = availableConnectionCount;
	}

	public Integer getBlockingTimeoutMillis() {
		return blockingTimeoutMillis;
	}

	public void setBlockingTimeoutMillis(Integer blockingTimeoutMillis) {
		this.blockingTimeoutMillis = blockingTimeoutMillis;
	}

	public Integer getConnectionCount() {
		return connectionCount;
	}

	public void setConnectionCount(Integer connectionCount) {
		this.connectionCount = connectionCount;
	}

	public Integer getConnectionCreatedCount() {
		return connectionCreatedCount;
	}

	public void setConnectionCreatedCount(Integer connectionCreatedCount) {
		this.connectionCreatedCount = connectionCreatedCount;
	}

	public Integer getConnectionDestroyedCount() {
		return connectionDestroyedCount;
	}

	public void setConnectionDestroyedCount(Integer connectionDestroyedCount) {
		this.connectionDestroyedCount = connectionDestroyedCount;
	}

	public Long getConnectionTimeoutMinutes() {
		return connectionTimeoutMinutes;
	}

	public void setConnectionTimeoutMinutes(Long connectionTimeoutMinutes) {
		this.connectionTimeoutMinutes = connectionTimeoutMinutes;
	}

	public Long getIdleTimeoutMinutes() {
		return idleTimeoutMinutes;
	}

	public void setIdleTimeoutMinutes(Long idleTimeoutMinutes) {
		this.idleTimeoutMinutes = idleTimeoutMinutes;
	}

	public Long getInUseConnectionCount() {
		return inUseConnectionCount;
	}

	public void setInUseConnectionCount(Long inUseConnectionCount) {
		this.inUseConnectionCount = inUseConnectionCount;
	}

	public Long getMaxConnectionsInUseCount() {
		return maxConnectionsInUseCount;
	}

	public void setMaxConnectionsInUseCount(Long maxConnectionsInUseCount) {
		this.maxConnectionsInUseCount = maxConnectionsInUseCount;
	}

	public Integer getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}

	public Integer getMinSize() {
		return minSize;
	}

	public void setMinSize(Integer minSize) {
		this.minSize = minSize;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getStateString() {
		return stateString;
	}

	public void setStateString(String stateString) {
		this.stateString = stateString;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}
}
