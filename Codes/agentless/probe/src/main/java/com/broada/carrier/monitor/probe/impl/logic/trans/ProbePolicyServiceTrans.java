package com.broada.carrier.monitor.probe.impl.logic.trans;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.impl.dao.PolicyDao;
import com.broada.carrier.monitor.probe.impl.entity.ProbeSideMonitorPolicy;
import com.broada.carrier.monitor.probe.impl.logic.ProbePolicyServiceEx;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;

public class ProbePolicyServiceTrans implements ProbePolicyServiceEx {
	@Autowired
	private PolicyDao dao;

	@Override
	public void deleteAll() {
		dao.deleteAll();
	}

	@Override
	public void savePolicy(MonitorPolicy policy) {
		dao.save(new ProbeSideMonitorPolicy(policy));
	}

	@Override
	public OperatorResult deletePolicy(String policyCode) {
		dao.delete(policyCode);
		return OperatorResult.DELETED;
	}

	@Override
	public MonitorPolicy[] getPolicies() {
		return dao.getAll();
	}

	@Override
	public MonitorPolicy getPolicy(String policyCode) {
		return dao.get(policyCode);
	}

}
