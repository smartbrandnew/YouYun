package com.broada.carrier.monitor.probe.impl.logic.trans;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.api.service.ProbeMethodService;
import com.broada.carrier.monitor.probe.impl.dao.MethodDao;
import com.broada.carrier.monitor.probe.impl.entity.ProbeSideMonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;

public class ProbeMethodServiceTrans implements ProbeMethodService {
	@Autowired
	private MethodDao dao;

	public void deleteAll() {
		dao.deleteAll();
	}

	@Override
	public void saveMethod(MonitorMethod method) {
		dao.save(new ProbeSideMonitorMethod(method));
	}

	@Override
	public OperatorResult deleteMethod(String methodCode) {
		dao.delete(methodCode);
		return OperatorResult.DELETED;
	}

	@Override
	public MonitorMethod[] getMethods() {
		return dao.getAll();
	}

	@Override
	public MonitorMethod getMethod(String methodCode) {
		return dao.get(methodCode);
	}

}
