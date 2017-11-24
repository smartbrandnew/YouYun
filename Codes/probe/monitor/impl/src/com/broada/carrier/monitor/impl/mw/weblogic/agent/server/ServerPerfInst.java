package com.broada.carrier.monitor.impl.mw.weblogic.agent.server;

/**
 * 
 * server性能监测的实体类
 * 
 * @author Yaojj Create By Mar 25, 2010 4:56:41 PM
 */
public class ServerPerfInst {
	/* server名称 */
	private String serverName;
	/* server状态 */
	private String state;
	/* server运行状态 */
	private String healthState;
	/* 空闲线程数 */
	private int executeThreadCurrentIdleCount;
	/* 请求队列大小 */
	private int pendingRequestCurrentCount;
	/* 内存使用量 */
	private double memoryUsage;

	private String instKey;

	/* 是否监控 */
	private boolean isMonitored = true;
	/* 空闲线程数阈值 */
	private int ideThreadThreshold = 300;
	/* 请求队列大小阈值 */
	private int requestThreshold = 0;

	public String getState() {
		return state == null ? "" : state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getHealthState() {
		return healthState == null ? "" : healthState;
	}

	public void setHealthState(String healthState) {
		this.healthState = healthState;
	}

	public int getExecuteThreadCurrentIdleCount() {
		return executeThreadCurrentIdleCount;
	}

	public void setExecuteThreadCurrentIdleCount(int executeThreadCurrentIdleCount) {
		this.executeThreadCurrentIdleCount = executeThreadCurrentIdleCount;
	}

	public int getPendingRequestCurrentCount() {
		return pendingRequestCurrentCount;
	}

	public void setPendingRequestCurrentCount(int pendingRequestCurrentCount) {
		this.pendingRequestCurrentCount = pendingRequestCurrentCount;
	}

	public double getMemoryUsage() {
		return memoryUsage;
	}

	public void setMemoryUsage(double memoryUsage) {
		this.memoryUsage = memoryUsage;
	}

	public String getServerName() {
		return serverName == null ? "" : serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getInstKey() {
		return instKey == null ? "" : instKey;
	}

	public void setInstKey(String instKey) {
		this.instKey = instKey;
	}

	public boolean isMonitored() {
		return isMonitored;
	}

	public void setMonitored(boolean isMonitored) {
		this.isMonitored = isMonitored;
	}

	public int getIdeThreadThreshold() {
		return ideThreadThreshold;
	}

	public void setIdeThreadThreshold(int ideThreadThreshold) {
		this.ideThreadThreshold = ideThreadThreshold;
	}

	public int getRequestThreshold() {
		return requestThreshold;
	}

	public void setRequestThreshold(int requestThreshold) {
		this.requestThreshold = requestThreshold;
	}
}
