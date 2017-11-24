package com.broada.carrier.monitor.probe.impl.dao;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.db.BaseDao;
import com.broada.carrier.monitor.common.db.PrepareQuery;
import com.broada.carrier.monitor.probe.impl.entity.ProbeSideMonitorTask;

public class TaskDao {
	@Autowired
	private BaseDao dao;

	public void save(ProbeSideMonitorTask task) {
		ProbeSideMonitorTask t = get(task.getId());
		if (t != null)
			task.setModified(t.getModified() == null ? new Date() : t
					.getModified());
		dao.save(task);
	}

	public void delete(String id) {
		dao.delete(ProbeSideMonitorTask.class, id);
	}


	public ProbeSideMonitorTask[] getAll() {
		return dao.queryForArray("from ProbeSideMonitorTask",
				new ProbeSideMonitorTask[0]);
	}

	public void deleteAll() {
		dao.execute("delete from ProbeSideMonitorTask");
	}

	public ProbeSideMonitorTask[] getByNodeId(String nodeId) {
		PrepareQuery query = new PrepareQuery("from ProbeSideMonitorTask where");
		query.append("nodeId = ", nodeId);
		return dao.queryForArray(query, new ProbeSideMonitorTask[0]);

	}

	public String[] getAllTaskIds() {
		return dao.queryForArray("select id from ProbeSideMonitorTask",
				new String[] {});
	}

	public ProbeSideMonitorTask get(String taskId) {
		return dao.get(ProbeSideMonitorTask.class, taskId);
	}

	public ProbeSideMonitorTask[] getByPolicyCode(String policyCode) {
		PrepareQuery query = new PrepareQuery(
				"from ProbeSideMonitorTask t where");
		query.append("policyCode = ", policyCode);
		return dao.queryForArray(query, new ProbeSideMonitorTask[0]);
	}

}
