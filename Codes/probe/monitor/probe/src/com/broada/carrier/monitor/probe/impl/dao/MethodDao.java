package com.broada.carrier.monitor.probe.impl.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.db.BaseDao;
import com.broada.carrier.monitor.probe.impl.entity.ProbeSideMonitorMethod;

public class MethodDao {
	@Autowired
	private BaseDao dao;

	public void deleteAll() {
		dao.execute("delete from ProbeSideMonitorMethod");
	}

	public void save(ProbeSideMonitorMethod method) {
		dao.save(method);
	}

	public void delete(String methodCode) {
		dao.delete(ProbeSideMonitorMethod.class, methodCode);
	}

	public ProbeSideMonitorMethod[] getAll() {
		return dao.queryForArray("from ProbeSideMonitorMethod", new ProbeSideMonitorMethod[0]);
	}

	public ProbeSideMonitorMethod get(String methodCode) {
		return dao.get(ProbeSideMonitorMethod.class, methodCode);
	}
}
