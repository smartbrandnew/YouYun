package com.broada.carrier.monitor.server.api.entity;

import java.io.Serializable;
import java.util.Date;

public class MonitorTargetStatus implements Serializable {
	private static final long serialVersionUID = 1L;
	private String targetId;
	private int taskCount;
	private MonitorState monitorState;
	private Date lastMonitorTime;

	public MonitorTargetStatus() {
	}

	public MonitorTargetStatus(String targetId, int taskCount, MonitorState monitorState, Date lastMonitorTime) {
		this.targetId = targetId;
		this.taskCount = taskCount;
		this.monitorState = monitorState;
		this.lastMonitorTime = lastMonitorTime;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public int getTaskCount() {
		return taskCount;
	}

	public void setTaskCount(int taskCount) {
		this.taskCount = taskCount;
	}

	public MonitorState getMonitorState() {
		return monitorState;
	}

	public void setMonitorState(MonitorState monitorState) {
		this.monitorState = monitorState;
	}

	public Date getLastMonitorTime() {
		return lastMonitorTime;
	}

	public void setLastMonitorTime(Date lastMonitorTime) {
		this.lastMonitorTime = lastMonitorTime;
	}

	public void set(MonitorTargetStatus copy) {
		this.targetId = copy.targetId;
		this.taskCount = copy.taskCount;
		this.monitorState = copy.monitorState;
		this.lastMonitorTime = copy.lastMonitorTime;
	}

}
