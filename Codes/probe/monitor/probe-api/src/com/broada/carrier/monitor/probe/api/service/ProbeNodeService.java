package com.broada.carrier.monitor.probe.api.service;

import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.service.BaseNodeService;

public interface ProbeNodeService extends BaseNodeService {	
	MonitorNode[] getNodes();
	
	String saveNode(MonitorNode node);
	
	OperatorResult deleteNode(String nodeId);

	MonitorNode getNode(String nodeId);
}
