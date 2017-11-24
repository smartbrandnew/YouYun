package com.broada.carrier.monitor.probe.impl.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.db.BaseDao;
import com.broada.carrier.monitor.probe.impl.entity.ProbeSideMonitorNode;

public class NodeDao {
	@Autowired
	private BaseDao dao;
	
	public void deleteAll() {		
		dao.execute("delete from ProbeSideMonitorNode");
	}

	public void save(ProbeSideMonitorNode node) {		
		dao.save(node);
	}

	public void delete(String nodeId) {
		dao.delete(ProbeSideMonitorNode.class, nodeId);
	}

	public ProbeSideMonitorNode[] getAll() {
		return dao.queryForArray("from ProbeSideMonitorNode", new ProbeSideMonitorNode[0]);
	}

	public ProbeSideMonitorNode get(String nodeId) {
		return dao.get(ProbeSideMonitorNode.class, nodeId);
	}

}
