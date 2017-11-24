package com.broada.carrier.monitor.probe.impl.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.db.BaseDao;
import com.broada.carrier.monitor.probe.impl.entity.ProbeSideMonitorRecord;

public class RecordDao {
	@Autowired
	private BaseDao dao;

	public ProbeSideMonitorRecord get(String taskId) {
		return dao.get(ProbeSideMonitorRecord.class, taskId);
	}

	public void save(ProbeSideMonitorRecord record) {
		dao.save(record);
	}
}
