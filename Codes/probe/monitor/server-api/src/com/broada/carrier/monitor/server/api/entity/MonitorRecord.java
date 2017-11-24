package com.broada.carrier.monitor.server.api.entity;

import java.io.Serializable;
import java.util.Date;

import com.broada.carrier.monitor.common.util.TextUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 监测任务运行记录
 * @author Jiangjw
 */
public class MonitorRecord implements Serializable {
	private static final long serialVersionUID = 1L;
	private String taskId;
	private MonitorState state;
	private MonitorState lastState;
	private Date time;
	private String message;

	public MonitorRecord() {
		this.state = MonitorState.UNMONITOR;
		this.lastState = MonitorState.UNMONITOR;
	}

	public MonitorRecord(MonitorRecord copy) {
		this(copy.getTaskId(), copy.getState(), copy.getTime(), copy.getMessage(), copy.getLastState());
	}

	public MonitorRecord(String taskId) {
		this(taskId, MonitorState.UNMONITOR, new Date(), MonitorState.UNMONITOR.getDescr(), null);
	}

	public MonitorRecord(String taskId, MonitorState state, Date time, String message, MonitorState lastState) {		
		this.taskId = taskId;
		this.state = state;
		this.lastState = lastState;
		this.time = time;
		this.message = message;
	}

	public MonitorState getLastState() {
		return lastState;
	}

	public void setLastState(MonitorState lastState) {
		this.lastState = lastState;
	}

	
	public MonitorState getState() {
		return state;
	}
	
	@JsonIgnore
	public boolean isStateChanged() {
		return state != lastState;
	}

	public void setState(MonitorState state) {
		this.state = state;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = TextUtil.truncate(message, 2000);
	}
	
	

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public void set(MonitorResult result, MonitorState lastState) {
		setTaskId(result.getTaskId());
		setTime(result.getTime());
		setState(result.getState());
		setMessage(result.getMessage());
		setLastState(lastState);
	}
}
