package com.broada.carrier.monitor.server.impl.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.db.BaseDao;
import com.broada.carrier.monitor.common.db.PrepareQuery;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;

public class InstanceDao {
	@Autowired
	private BaseDao dao;

	public void save(MonitorInstance instance) {
		dao.save(instance);
	}

	public void deleteByTaskId(String taskId) {
		dao.execute("delete from MonitorInstance where taskId = ?1", taskId);
	}

	public MonitorInstance[] getByTaskId(String taskId) {
		PrepareQuery query = new PrepareQuery("from MonitorInstance where");
		query.append("taskId = ", taskId);
		return dao.queryForArray(query, new MonitorInstance[0]);
	}

}
