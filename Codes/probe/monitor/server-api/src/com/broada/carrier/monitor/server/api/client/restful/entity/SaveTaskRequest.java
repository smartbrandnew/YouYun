package com.broada.carrier.monitor.server.api.client.restful.entity;

import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;

public class SaveTaskRequest {
	private MonitorTask task;
	private boolean withInstances;
	private MonitorInstance[] instances;

	public SaveTaskRequest() {
	}

	public SaveTaskRequest(MonitorTask task) {
		this.task = task;
		this.withInstances = false;
	}

	public SaveTaskRequest(MonitorTask task, MonitorInstance[] instances) {
		this.task = task;
		this.withInstances = true;
		this.instances = instances;
	}

	public MonitorTask getTask() {
		return task;
	}

	public void setTask(MonitorTask task) {
		this.task = task;
	}

	public MonitorInstance[] getInstances() {
		return instances;
	}

	public void setInstances(MonitorInstance[] instances) {
		this.instances = instances;
	}

	public boolean isWithInstances() {
		return withInstances;
	}

	public void setWithInstances(boolean withInstances) {
		this.withInstances = withInstances;
	}

}
