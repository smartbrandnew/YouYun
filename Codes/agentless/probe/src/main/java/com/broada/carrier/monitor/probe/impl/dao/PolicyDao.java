package com.broada.carrier.monitor.probe.impl.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.db.BaseDao;
import com.broada.carrier.monitor.probe.impl.entity.ProbeSideMonitorPolicy;

public class PolicyDao {
	@Autowired

	private BaseDao dao;
	public void deleteAll() {
		dao.execute("delete from ProbeSideMonitorPolicy");
	}

	public void save(ProbeSideMonitorPolicy policy) {
		dao.save(policy);
	}

	public void delete(String policyCode) {
		dao.delete(ProbeSideMonitorPolicy.class, policyCode);
	}

	public ProbeSideMonitorPolicy[] getAll() {
		return dao.queryForArray("from ProbeSideMonitorPolicy", new ProbeSideMonitorPolicy[0]);
	}

	public ProbeSideMonitorPolicy get(String policyCode) {
		return dao.get(ProbeSideMonitorPolicy.class, policyCode);
	}
}
