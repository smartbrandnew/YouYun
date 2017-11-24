package com.broada.carrier.monitor.server.impl.dao;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.db.BaseDao;
import com.broada.carrier.monitor.common.db.PrepareQuery;
import com.broada.carrier.monitor.server.impl.entity.ServerSideMonitorNode;

public class NodeDao {
	@Autowired
	private BaseDao dao;;

	public ServerSideMonitorNode[] getByProbeId(String domainId, int probeId) {
		PrepareQuery query;
		if (domainId == null) {
			query = new PrepareQuery("select n from ServerSideMonitorNode n");
			query.append("where");
		} else {
			query = new PrepareQuery("select n from ServerSideMonitorNode n, DomainObjectMap m");
			query.append("where n.id = m.key.targetId");
			query.append("and m.key.domainId = ", domainId);
			query.append("and");
		}
		query.append("n.probeId = ", probeId);
		return dao.queryForArray(query, new ServerSideMonitorNode[0]);
	}
	
	public ServerSideMonitorNode[] getNodes(String domainId) {
		PrepareQuery query;
		if(domainId == null){
			query = new PrepareQuery("select n from ServerSideMonitorNode n");
		} else{
			query = new PrepareQuery("select n from ServerSideMonitorNode n, DomainObjectMap m");
			query.append("where n.id = m.key.targetId");
			query.append("and m.key.domainId = ", domainId);
		}
		return dao.queryForArray(query, new ServerSideMonitorNode[0]);
	}
	
	public ServerSideMonitorNode[] getNodes(List<String> ids, String domainId) {
		PrepareQuery query;
		if(ids == null || ids.size() == 0)
			return new ServerSideMonitorNode[0];
		
		int maxQuery = 1000;//oracle数据库对in查询做了限制，最多1000个，mysql无限制
		StringBuffer sb = new StringBuffer();
		int size = ids.size() % maxQuery;
		if(size == 0)
			size = ids.size() / maxQuery - 1;
		else
			size = ids.size() / maxQuery;
		
		//组成n.id in () or n.id in () or ....
		for(int i = 0; i <= size; i++){
			if(i == 0)
				sb.append(" n.id in ('");
			else
				sb.append(" or n.id in ('");
			if(i != size)
				sb.append(StringUtils.join(ids.subList(i * maxQuery, i * maxQuery + maxQuery), "','"));
			else
				sb.append(StringUtils.join(ids.subList(i * maxQuery, ids.size()), "','"));
			sb.append("')");
		}
		
		if(domainId == null){
			query = new PrepareQuery("select n from ServerSideMonitorNode n");
			query.append("where " + sb.toString());
		} else{
			query = new PrepareQuery("select n from ServerSideMonitorNode n, DomainObjectMap m");
			query.append("where n.id = m.key.targetId");
			query.append("and m.key.domainId = ", domainId);
			query.append("and (" + sb.toString() + ")");
		}
		return dao.queryForArray(query, new ServerSideMonitorNode[0]);
	}

	public void save(ServerSideMonitorNode node) {
		dao.save(node);
	}

	public void delete(String id) {
		dao.delete(ServerSideMonitorNode.class, id);
	}

	public ServerSideMonitorNode get(String id) {
		return dao.get(ServerSideMonitorNode.class, id);
	}
	
	public int[] getProbeIdsByMethodCode(String methodCode) {
		return getProbeIdsByCondition("and t.methodCode = ", methodCode);		
	}

	private int[] getProbeIdsByCondition(String condition, String value) {
		PrepareQuery query = new PrepareQuery("select n.probeId from ServerSideMonitorNode n, ServerSideMonitorTask t where n.id = t.nodeId");
		query.append(condition, value);
		query.append("group by n.probeId");
		Integer[] ids = dao.queryForArray(query, new Integer[0]);
		int[] result = new int[ids.length];
		for (int i = 0; i < result.length; i++)
			result[i] = ids[i];
		return result;
	}

	public int[] getProbeIdsByPolicyCode(String policyCode) {
		return getProbeIdsByCondition("and t.policyCode = ", policyCode);
	}
}
