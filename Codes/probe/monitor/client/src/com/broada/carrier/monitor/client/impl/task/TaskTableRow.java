package com.broada.carrier.monitor.client.impl.task;

import java.util.Date;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;

public class TaskTableRow {
	private MonitorTask task;
	private MonitorNode node;
	private MonitorResource resource;
	private MonitorRecord record;

	public TaskTableRow(MonitorTask task, MonitorNode node, MonitorResource resource, MonitorRecord record) {
		this.task = task;
		this.node = node;
		this.resource = resource;
		this.record = record;
	}

	public void setRecord(MonitorRecord record) {
		this.record = record;
	}

	public TaskTableRow(MonitorTask task) {
		this.task = task;				
	}

	public MonitorTask getTask() {
		return task;
	}

	public boolean isEnabled() {
		return task.isEnabled();
	}	

	public MonitorRecord getRecord() {
		if (record == null)
			this.record = ServerContext.getTaskService().getRecord(task.getId());
		return record;
	}

	public MonitorNode getNode() {
		if (node == null) {
			node = ServerContext.getNode(task.getNodeId());
			if (node == null) {
				node = new MonitorNode();
				node.setName("已删除");
				node.setIp("");
			}
		}
		return node;
	}

	public MonitorResource getResource() {
		if (resource == null) {
			if (task.getResourceId() != null) {
				resource = ServerContext.getResource(task.getResourceId());
				if (resource == null) {
					resource = new MonitorResource();
					resource.setName("已删除");
				}
			}
		}		
		return resource;
	}

	public String getNodeIp() {
		return getNode().getIp();
	}

	public String getNodeName() {
		return getNode().getName();
	}

	public String getResourceName() {
		if (getResource() == null)
			return null;
		else
			return getResource().getName();
	}

	public String getName() {
		return task.getName();
	}

	public Date getLastRunTime() {
		return getRecord().getTime();
	}

	public MonitorState getState() {
		return getRecord().getState();
	}

	@Override
	public int hashCode() {
		return task.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		TaskTableRow other = (TaskTableRow) obj;
		return task.equals(other.task);
	}
}