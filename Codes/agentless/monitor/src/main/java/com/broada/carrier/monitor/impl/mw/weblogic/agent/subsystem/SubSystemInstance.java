package com.broada.carrier.monitor.impl.mw.weblogic.agent.subsystem;

/**
 * 子系统
 * 
 * @author Yaojj Create By Mar 29, 2010 9:37:11 AM
 */
public class SubSystemInstance {
	/* 子系统名称 */
	private String subSystem;
	/* 子系统状态 */
	private String state;
	/* 子系统状态原因 */
	private String reasonCode;
	/* 是否监控 */
	private boolean isMonitored;
	/* 状态阈值 */
	private String thresholdState = "OK";

	private String instKey;

	public String getInstKey() {
		return instKey;
	}

	public void setInstKey(String instKey) {
		this.instKey = instKey;
	}

	public String getSubSystem() {
		return subSystem;
	}

	public void setSubSystem(String subSystem) {
		this.subSystem = subSystem;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}

	public boolean isMonitored() {
		return isMonitored;
	}

	public void setMonitored(boolean isMonitored) {
		this.isMonitored = isMonitored;
	}

	public String getThresholdState() {
		return thresholdState;
	}

	public void setThresholdState(String thresholdState) {
		this.thresholdState = thresholdState;
	}

}
