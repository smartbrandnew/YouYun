package com.broada.carrier.monitor.client.impl.impexp.entity;

import java.util.ArrayList;
import java.util.List;

import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ImpExpTask extends MonitorTask {
	private static final long serialVersionUID = 1L;
	private String nodeIp;
	private String resourceName;
	private List<MonitorInstance> instances = new ArrayList<MonitorInstance>();

	@JsonIgnore
	public String getNodeIp() {
		return nodeIp;
	}

	public void setNodeIp(String nodeIp) {
		this.nodeIp = nodeIp;
	}

	@JsonIgnore
	public String getResourceName() {
		return resourceName;
	}

	@JsonIgnore
	public List<MonitorInstance> getInstances() {
		return instances;
	}

	public void setResourceName(String resourceName) {
		if (!(this.resourceName == resourceName
				|| resourceName == null
				|| resourceName.isEmpty()))
			this.resourceName = resourceName;
	}

	@Override
	public String toString() {
		return String.format("%s[nodeIp: %s resourceName: %s name: %s type: %s method: %s policy: %s instances: %d]",
				getClass().getSimpleName(), nodeIp, resourceName, getName(), getTypeId(), getMethodCode(), getPolicyCode(),
				instances.size());
	}

	public void add(MonitorInstance instance) {
		instances.add(instance);
	}

	@JsonIgnore
	public String getDisplayName() {
		return String.format("节点[%s] 资源[%s] 监测任务[%s]", nodeIp, resourceName, getName());
	}
}
