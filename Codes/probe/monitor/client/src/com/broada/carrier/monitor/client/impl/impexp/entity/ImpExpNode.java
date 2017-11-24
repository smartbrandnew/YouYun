package com.broada.carrier.monitor.client.impl.impexp.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.broada.carrier.monitor.client.impl.impexp.util.Logger;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ImpExpNode extends MonitorNode {
	private static final long serialVersionUID = 1L;
	private String probeCode;
	private Map<String, ImpExpResource> resources = new LinkedHashMap<String, ImpExpResource>(5);

	@JsonIgnore
	public String getProbeCode() {
		return probeCode;
	}

	public void setProbeCode(String probeCode) {
		this.probeCode = probeCode;
	}

	public void addResource(ImpExpResource resource) {
		if (!resources.containsKey(resource.getName())) {
			resources.put(resource.getName(), resource);
		} else
			Logger.log(new Log(LogLevel.WARN, "资源名称重复，将优先使用第1个：" + resource.getName()));
	}

	@JsonIgnore
	public Map<String, ImpExpResource> getResources() {
		return resources;
	}

	public ImpExpResource getResource(String name) {
		return resources.get(name);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		for (ImpExpResource resource : resources.values())
			sb.append("\n\t").append(resource);
		return sb.toString();
	}

	public MonitorResource checkResource(String resourceName) {		
		MonitorResource result = null;
		if (resourceName != null && !resourceName.isEmpty()) {
			result = resources.get(resourceName);
			if (result == null)
				throw new IllegalArgumentException(String.format("监测资源不存在[%s-%s]", getIp(), resourceName));
		}
		return result;
	}
}
