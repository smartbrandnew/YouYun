package com.broada.carrier.monitor.spi.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.MonitorType;

public class MonitorConfigContext {
	private ServerServiceFactory serverFactory;
	private MonitorNode node;
	private MonitorResource resource;
	private MonitorTask task;
	private Map<String, MonitorInstance> instances = new LinkedHashMap<String, MonitorInstance>();

	public MonitorConfigContext(ServerServiceFactory serverFactory, MonitorNode node, MonitorResource resource,
			MonitorTask task, MonitorInstance[] instances) {
		this.serverFactory = serverFactory;
		this.node = node;
		this.resource = resource;
		this.task = task;
		if (instances != null) {
			for (MonitorInstance inst : instances)
				this.instances.put(inst.getCode(), inst);
		}
	}

	public ServerServiceFactory getServerFactory() {
		return serverFactory;
	}

	public MonitorTask getTask() {
		return task;
	}

	public MonitorInstance[] getInstances() {
		return instances.values().toArray(new MonitorInstance[0]);
	}

	public MonitorNode getNode() {
		return node;
	}

	public MonitorResource getResource() {
		return resource;
	}

	public void setInstances(MonitorInstance[] instances) {
		this.instances.clear();
		for (MonitorInstance inst : instances)
			this.instances.put(inst.getCode(), inst);
	}

	public int getInstanceCount() {
		return instances == null ? 0 : instances.size();
	}

	public void addInstance(MonitorInstance inst) {
		instances.put(inst.getCode(), inst);
	}

	public MonitorInstance removeInstance(String instCode) {
		return instances.remove(instCode);
	}

	public void removeInstanceAll() {
		instances.clear();
	}

	public MonitorInstance getInstance(String instCode) {
		return instances.get(instCode);
	}

	public MonitorType getType() {
		if (task.getTypeId() == null)
			return null;
		return ServerUtil.checkType(serverFactory.getTypeService(), task.getTypeId());
	}

	public void setResource(MonitorResource resource) {
		this.resource = resource;
	}

	public void setTask(MonitorTask task) {
		this.task = task;
	}

}
