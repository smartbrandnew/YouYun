package com.broada.carrier.monitor.server.impl.dao;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.db.BaseDao;
import com.broada.carrier.monitor.common.db.PrepareQuery;
import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.impl.entity.ServerSideMonitorTask;

public class TaskDao {

	@Autowired
	private BaseDao dao;

	public Page<ServerSideMonitorTask> getByProbeId(String domainId, PageNo pageNo, int probeId) {
		PrepareQuery query = new PrepareQuery();
		if (domainId == null) {
			query.append("select t from ServerSideMonitorTask t, ServerSideMonitorNode n");
			query.append("where t.nodeId = n.id");
		} else {
			query.append("select t from ServerSideMonitorTask t, ServerSideMonitorNode n, DomainObjectMap m");
			query.append("where t.nodeId = n.id and n.id = m.key.targetId");
			query.append("and m.key.domainId = ", domainId);
		}
		query.append("and n.probeId = ", probeId);
		return dao.queryForPage(query, pageNo, new ServerSideMonitorTask[0]);
	}

	public ServerSideMonitorTask[] getByNodeId(String nodeId) {
		PrepareQuery query = new PrepareQuery("from ServerSideMonitorTask where nodeId = ?1", nodeId);
		return dao.queryForArray(query, new ServerSideMonitorTask[0]);
	}
	
	public ServerSideMonitorTask[] getByNodeIds(String[] nodeId) {
		if(nodeId == null || nodeId.length == 0)
			return new ServerSideMonitorTask[0];
		PrepareQuery query = new PrepareQuery("from ServerSideMonitorTask where nodeId in");
		query.append("('" + StringUtils.join(nodeId, "','") + "')");
		return dao.queryForArray(query, new ServerSideMonitorTask[0]);
	}

	public ServerSideMonitorTask[] getByResourceId(String resourceId) {
		PrepareQuery query = new PrepareQuery("from ServerSideMonitorTask where resourceId = ?1", resourceId);
		return dao.queryForArray(query, new ServerSideMonitorTask[0]);
	}
	
	public ServerSideMonitorTask[] getByResourceIds(String[] resourceIds) {
		if(resourceIds == null || resourceIds.length == 0)
			return new ServerSideMonitorTask[0];
		PrepareQuery query = new PrepareQuery("from ServerSideMonitorTask where resourceId in");
		query.append("('" + StringUtils.join(resourceIds, "','") + "')");
		return dao.queryForArray(query, new ServerSideMonitorTask[0]);
	}

	public String save(ServerSideMonitorTask task) {
		task.setModified(new Date());
		if (task.getId() == null||task.getId().trim().length()<=0)
			dao.create(task);
		else
			dao.save(task);
		return task.getId();
	}

	public void delete(String id) {
		dao.delete(ServerSideMonitorTask.class, id);
	}

	public ServerSideMonitorTask get(String id) {
		return dao.get(ServerSideMonitorTask.class, id);
	}

	public Page<ServerSideMonitorTask> getAll(PageNo pageNo) {
		PrepareQuery query = new PrepareQuery("select t from ServerSideMonitorTask t");
		return dao.queryForPage(query, pageNo, new ServerSideMonitorTask[0]);
	}

	public Page<ServerSideMonitorTask> getAll(PageNo pageNo, String domainId) {
		PrepareQuery query = new PrepareQuery();
		if (domainId == null) {
			query.append("select t from ServerSideMonitorTask t");
		} else {
			query.append("select t from ServerSideMonitorTask t, ServerSideMonitorNode n, DomainObjectMap m");
			query.append("where t.nodeId = n.id and n.id = m.key.targetId");
			query.append("and m.key.domainId = ", domainId);
		}
		return dao.queryForPage(query, pageNo, new ServerSideMonitorTask[0]);
	}

	public Page<ServerSideMonitorTask> getByPolicyCode(PageNo pageNo, String policyCode) {
		PrepareQuery query = new PrepareQuery("select t from ServerSideMonitorTask t where t.policyCode = ?1", policyCode);
		return dao.queryForPage(query, pageNo, new ServerSideMonitorTask[0]);
	}

	public int getTaskPCServerCount() {
		getAll(PageNo.ONE);
		PrepareQuery query = new PrepareQuery(
				"select count(*) from (\r\n" +
						"select distinct t.node_id \r\n" +
						"from mon_task t\r\n" +
						"inner join res_object_real o on t.node_id = o.id\r\n" +
						"inner join res_class c on o.class_id = c.id\r\n" +
						"left join res_object_real ro on t.res_id = ro.id\r\n" +
						"left join res_class rc on ro.class_id = rc.id\r\n" +
						"where c.path like 'BaseDevice/Computer%'  \r\n" +
						"    and (not c.path like 'BaseDevice/Computer/Server%'\r\n" +
						"    or c.path = 'BaseDevice/Computer/Server/PCServer')\r\n" +
						"    and (rc.path like 'AppPlatform/OS%' or rc.path is null)\r\n" +
						") a     ");
		query.setNativeSql(true);
		Number num = (Number) dao.queryForObject(query);
		if (num != null)
			return num.intValue();
		return 0;
	}

	public int getTaskStorageDevCount() {
		getAll(PageNo.ONE);
		PrepareQuery query = new PrepareQuery(
				"select count(*) from (\r\n" +
						"select distinct t.node_id \r\n" +
						"from mon_task t\r\n" +
						"inner join res_object_real o on t.node_id = o.id\r\n" +
						"inner join res_class c on o.class_id = c.id\r\n" +
						"where c.path like 'BaseDevice/StorageDev%' \r\n" +
						") a    ");
		query.setNativeSql(true);
		Number num = (Number) dao.queryForObject(query);
		if (num != null)
			return num.intValue();
		return 0;
	}

	public int getTaskMiniServerCount() {
		getAll(PageNo.ONE);
		PrepareQuery query = new PrepareQuery(
				"select count(*) from (\r\n" +
						"select distinct t.node_id \r\n" +
						"from mon_task t\r\n" +
						"inner join res_object_real o on t.node_id = o.id\r\n" +
						"inner join res_class c on o.class_id = c.id\r\n" +
						"left join res_object_real ro on t.res_id = ro.id\r\n" +
						"left join res_class rc on ro.class_id = rc.id\r\n" +
						"where c.path like 'BaseDevice/Computer/Server%' \r\n" +
						"    and c.path <> 'BaseDevice/Computer/Server/PCServer'\r\n" +
						"    and (rc.path like 'AppPlatform/OS%' or rc.path is null)\r\n" +
						") a   ");
		query.setNativeSql(true);
		Number num = (Number) dao.queryForObject(query);
		if (num != null)
			return num.intValue();
		return 0;
	}

	public int getTaskDeviceCountByHaveRes() {
		// 此getAll是为了修正一个jpa的坑，不知道为何，下一句select count(*)无法直接统计到事务内的数据，所以增加这句getAll即可
		getAll(PageNo.ONE);
		PrepareQuery query = new PrepareQuery(
				"select count(*) from (  select t.node_id from mon_task t\r\n" +
						"    inner join res_object_real o on t.res_id = o.id\r\n" +
						" inner join res_class c on o.class_id = c.id \r\n" +
						" where not c.path like 'AppPlatform/OS%' group by t.node_id) a  ");
		query.setNativeSql(true);
		Number num = (Number) dao.queryForObject(query);
		if (num != null)
			return num.intValue();
		return 0;
	}

	public String[] getMethodCodesByNodeId(String nodeId) {
		PrepareQuery query = new PrepareQuery("select t.methodCode from ServerSideMonitorTask t where");
		query.append("t.nodeId = ", nodeId);
		return dao.queryForArray(query, new String[0]);
	}

	public String[] getMethodCodesByResourceId(String resourceId) {
		PrepareQuery query = new PrepareQuery("select t.methodCode from ServerSideMonitorTask t where");
		query.append("t.resourceId = ", resourceId);
		return dao.queryForArray(query, new String[0]);
	}

	public String[] getResourceIds() {
		PrepareQuery query = new PrepareQuery("select resourceId from ServerSideMonitorTask group by resourceId");
		return dao.queryForArray(query, new String[0]);
	}

	public String[] getNodeIds() {
		PrepareQuery query = new PrepareQuery("select nodeId from ServerSideMonitorTask group by nodeId");
		return dao.queryForArray(query, new String[0]);
	}
}
