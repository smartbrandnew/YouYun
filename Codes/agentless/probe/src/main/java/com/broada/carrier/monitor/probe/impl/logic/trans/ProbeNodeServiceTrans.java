package com.broada.carrier.monitor.probe.impl.logic.trans;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.api.service.ProbeNodeService;
import com.broada.carrier.monitor.probe.impl.dao.NodeDao;
import com.broada.carrier.monitor.probe.impl.entity.ProbeSideMonitorNode;
import com.broada.carrier.monitor.probe.impl.logic.ProbeTaskServiceEx;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;

public class ProbeNodeServiceTrans implements ProbeNodeService {
	@Autowired
	private NodeDao dao;
	@Autowired
	private ProbeResourceServiceTrans resourceManager;
	@Autowired	
	private ProbeTaskServiceEx taskService;

	@Override
	public String saveNode(MonitorNode node) {
		dao.save(new ProbeSideMonitorNode(node));
		return node.getId();
	}

	@Override
	public OperatorResult deleteNode(String nodeId) {
		dao.delete(nodeId);
		return OperatorResult.DELETED;
	}

	public void deleteAll() {
		dao.deleteAll();
		resourceManager.deleteAll();
		taskService.deleteAll();
	}

	@Override
	public MonitorNode[] getNodes() {
		return dao.getAll();
	}

	@Override
	public MonitorNode getNode(String nodeId) {
		return dao.get(nodeId);
	}

}
