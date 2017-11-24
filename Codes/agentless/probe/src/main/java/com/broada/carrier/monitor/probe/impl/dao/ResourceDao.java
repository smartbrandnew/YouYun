package com.broada.carrier.monitor.probe.impl.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.db.BaseDao;
import com.broada.carrier.monitor.common.db.PrepareQuery;
import com.broada.carrier.monitor.probe.impl.entity.ProbeSideMonitorResource;

public class ResourceDao {
	@Autowired
	private BaseDao dao;
	
	public void deleteAll() {
		dao.execute("delete from ProbeSideMonitorResource");
	}

	public void save(ProbeSideMonitorResource resource) {
		dao.save(resource);
	}

	public void delete(String resourceId) {
		dao.delete(ProbeSideMonitorResource.class, resourceId);
	}

	public ProbeSideMonitorResource[] getAll() {
		return dao.queryForArray("from ProbeSideMonitorResource", new ProbeSideMonitorResource[0]);
	}

	public ProbeSideMonitorResource[] getByNodeId(String nodeId) {
		PrepareQuery query = new PrepareQuery("from ProbeSideMonitorResource where");
		query.append("nodeId = ", nodeId);
		return dao.queryForArray(query, new ProbeSideMonitorResource[0]);
	}

	public ProbeSideMonitorResource get(String resourceId) {
		return dao.get(ProbeSideMonitorResource.class, resourceId);
	}
}
