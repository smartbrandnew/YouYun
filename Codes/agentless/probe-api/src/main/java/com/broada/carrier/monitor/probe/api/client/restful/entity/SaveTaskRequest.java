package com.broada.carrier.monitor.probe.api.client.restful.entity;

import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;

public class SaveTaskRequest {
	private MonitorTask task;
	private MonitorInstance[] instances;
	private MonitorRecord record;

	public SaveTaskRequest() {
	}

	public SaveTaskRequest(MonitorTask task, MonitorInstance[] instances, MonitorRecord record) {
		this.task = task;
		this.instances = instances;
		this.record = record;
	}

	public MonitorInstance[] getInstances() {
		return instances;
	}

	public void setInstances(MonitorInstance[] instances) {
		this.instances = instances;
	}

	public MonitorTask getTask() {
		return task;
	}

	public void setTask(MonitorTask task) {
		this.task = task;
	}

	public MonitorRecord getRecord() {
		return record;
	}

	public void setRecord(MonitorRecord record) {
		this.record = record;
	}

}
