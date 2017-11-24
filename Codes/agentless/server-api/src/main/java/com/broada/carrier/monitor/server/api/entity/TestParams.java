package com.broada.carrier.monitor.server.api.entity;

import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;

public class TestParams {
	private MonitorNode node;
	private MonitorResource resource;
	private MonitorMethod method;
	private MonitorTask task;
	private MonitorInstance[] instances;

	public TestParams() {
	}

	public TestParams(MonitorNode node, MonitorResource resource, MonitorMethod method, MonitorTask task,
			MonitorInstance[] instances) {
		this.node = node;
		this.resource = resource;
		this.method = method;
		this.task = task;
		this.instances = instances;
	}

	public TestParams(TestParams copy) {
		this(copy.getNode(), copy.getResource(), copy.getMethod(), copy.getTask(), copy.getInstances());
	}

	public MonitorInstance[] getInstances() {
		return instances;
	}

	public MonitorNode getNode() {
		return node;
	}

	public MonitorResource getResource() {
		return resource;
	}

	public MonitorMethod getMethod() {
		return method;
	}

	public MonitorTask getTask() {
		return task;
	}

	public MonitorInstance getInstanceByCode(String code) {
		if (instances == null)
			return null;

		for (MonitorInstance instance : instances) {
			if (code == null && instance.getCode() == null)
				return instance;
			else if (code != null && code.equals(instance.getCode()))
				return instance;
		}

		return null;
	}

	public void setNode(MonitorNode node) {
		this.node = node;
	}

	public void setResource(MonitorResource resource) {
		this.resource = resource;
	}

	public void setMethod(MonitorMethod method) {
		this.method = method;
	}

	public void setTask(MonitorTask task) {
		this.task = task;
	}

	public void setInstances(MonitorInstance[] instances) {
		this.instances = instances;
	}

}
