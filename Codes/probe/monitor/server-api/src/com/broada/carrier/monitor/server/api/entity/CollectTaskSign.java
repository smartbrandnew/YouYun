package com.broada.carrier.monitor.server.api.entity;

import java.io.Serializable;


public class CollectTaskSign implements Serializable {
	private static final long serialVersionUID = 1L;
	private String taskId;
	private String nodeId;

	public CollectTaskSign() {
	}

	public CollectTaskSign(String taskId, String nodeId) {
		this.taskId = taskId;
		this.nodeId = nodeId;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

}
