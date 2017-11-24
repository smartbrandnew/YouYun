package com.broada.carrier.monitor.server.api.entity;

import java.io.Serializable;

import com.broada.carrier.monitor.common.util.AnyObject;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class CollectParams implements Serializable {
	private static final long serialVersionUID = 1L;
	private String typeId;
	private MonitorNode node;
	private MonitorResource resource;
	private MonitorMethod method;
	private MonitorInstance[] instances;
	private String parameter;

	public CollectParams() {
	}
	
	public CollectParams(CollectParams copy) {
		this(copy.typeId, copy.node, copy.resource, copy.method, copy.instances, copy.parameter);		
	}
	
	public CollectParams(String typeId, MonitorNode node, MonitorResource resource, MonitorMethod method,
			String parameter) {
		this(typeId, node, resource, method, null, parameter);
	}

	public CollectParams(String typeId, MonitorNode node, MonitorResource resource, MonitorMethod method,
			MonitorInstance[] instances,
			String parameter) {
		this.typeId = typeId;
		this.node = node;
		this.resource = resource;
		this.method = method;
		this.instances = instances;
		this.parameter = parameter;
	}

	public CollectParams(String typeId, MonitorNode node, MonitorResource resource, MonitorMethod method,
			MonitorInstance[] instances,
			Serializable parameter) {
		this(typeId, node, resource, method, instances, null);
		setParameterObject(parameter);
	}

	public CollectParams(String typeId, MonitorNode node, MonitorResource resource, MonitorMethod method,
			Serializable parameter) {
		this(typeId, node, resource, method, null, parameter);
	}

	/**
	 * 获取采集时需要考虑的监测实例
	 * @return
	 */
	public MonitorInstance[] getInstances() {
		return instances;
	}

	public void setInstances(MonitorInstance[] instances) {
		this.instances = instances;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getTypeId() {
		return typeId;
	}

	public MonitorNode getNode() {
		return node;
	}

	public void setNode(MonitorNode node) {
		this.node = node;
	}

	public MonitorResource getResource() {
		return resource;
	}

	public void setResource(MonitorResource resource) {
		this.resource = resource;
	}

	public MonitorMethod getMethod() {
		return method;
	}

	public void setMethod(MonitorMethod method) {
		this.method = method;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	@JsonIgnore
	public <T> T getParameterObject(Class<T> cls) {
		return AnyObject.decode(parameter, cls);
	}
	
	public void setParameterObject(Object param) {
		this.parameter = AnyObject.encode(param);
	}
}
