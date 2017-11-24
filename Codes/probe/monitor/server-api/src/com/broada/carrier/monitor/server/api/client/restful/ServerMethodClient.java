package com.broada.carrier.monitor.server.api.client.restful;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.service.ServerMethodService;

public class ServerMethodClient extends BaseMethodClient implements ServerMethodService {
	public ServerMethodClient(String baseServiceUrl) {
		super(baseServiceUrl, "/api/v1/monitor/methods");
	}

	@Override
	public MonitorMethod[] getMethodsByTypeId(String typeId) {
		return client.get(MonitorMethod[].class, "typeId", typeId);
	}

	@Override
	public void createMethod(MonitorMethod method) {
		client.post("0/create", null, method);
	}

	@Override
	public MonitorMethod[] getMethodsByNodeIdAndType(String nodeId, String typeId) {
		return client.get(MonitorMethod[].class, "nodeId", nodeId, "typeId", typeId);

	}

}
