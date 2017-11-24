package com.broada.carrier.monitor.spi.entity;

import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;

public class MonitorMethodConfigContext {
	private ServerServiceFactory serverFactory;
	private MonitorNode node;
	private MonitorResource resource;
	private MonitorMethod method;

	public MonitorMethodConfigContext(ServerServiceFactory serverFactory, MonitorNode node, MonitorResource resource,
			MonitorMethod method) {
		this.serverFactory = serverFactory;
		this.node = node;
		this.resource = resource;
		this.method = method;
	}

	public ServerServiceFactory getServerFactory() {
		return serverFactory;
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

}
