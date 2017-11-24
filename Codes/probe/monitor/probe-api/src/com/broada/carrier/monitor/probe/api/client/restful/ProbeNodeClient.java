package com.broada.carrier.monitor.probe.api.client.restful;

import com.broada.carrier.monitor.probe.api.service.ProbeNodeService;
import com.broada.carrier.monitor.server.api.client.restful.BaseNodeClient;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;

public class ProbeNodeClient extends BaseNodeClient implements ProbeNodeService {
	public ProbeNodeClient(String baseServiceUrl) {		
		super(baseServiceUrl, "/api/v1/probe/nodes");
	}
	
	@Override
	public MonitorNode[] getNodes() {
		return client.get(MonitorNode[].class);
	}
}
