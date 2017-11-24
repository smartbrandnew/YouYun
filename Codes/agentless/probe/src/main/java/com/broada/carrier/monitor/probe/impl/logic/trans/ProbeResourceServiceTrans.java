package com.broada.carrier.monitor.probe.impl.logic.trans;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.api.service.ProbeResourceService;
import com.broada.carrier.monitor.probe.impl.dao.ResourceDao;
import com.broada.carrier.monitor.probe.impl.entity.ProbeSideMonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;

public class ProbeResourceServiceTrans implements ProbeResourceService {
	@Autowired
	ResourceDao dao;
	
	public void deleteAll() {
		dao.deleteAll();
	}

	@Override
	public String saveResource(MonitorResource resource) {
		dao.save(new ProbeSideMonitorResource(resource));
		return resource.getId();
	}

	@Override
	public OperatorResult deleteResource(String resourceId) {
		dao.delete(resourceId);
		return OperatorResult.DELETED;
	}

	@Override
	public MonitorResource getResource(String resourceId) {
		return dao.get(resourceId);
	}

}
