package com.broada.carrier.monitor.server.api.client.restful;

import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.service.BaseNodeService;

public class BaseNodeClient extends BaseServiceClient implements BaseNodeService {
	public BaseNodeClient(String baseServiceUrl, String apiPath) {
		super(baseServiceUrl, apiPath);
	}
	
	@Override
	public MonitorNode getNode(String nodeId) {
		if (nodeId == null)
			throw new IllegalArgumentException();
		return client.get(nodeId, MonitorNode.class);
	}

	@Override
	public String saveNode(MonitorNode node) {
		return client.post(String.class, node);
	}

	@Override
	public OperatorResult deleteNode(String id) {
		return client.post(id + "/delete", OperatorResult.class);
	}
}
