package com.broada.carrier.monitor.server.impl.dao;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.db.BaseDao;
import com.broada.carrier.monitor.common.db.PrepareQuery;
import com.broada.carrier.monitor.server.impl.entity.LocalRemoteKey;
import com.broada.utils.StringUtil;

public class LocalRemoteKeyDao {
	@Autowired
	private BaseDao dao;

	@SuppressWarnings("unchecked")
	public Collection<LocalRemoteKey> getByLocalKey(String localKey) {
		PrepareQuery query = new PrepareQuery(
				"select m from LocalRemoteKey m, ServerSideMonitorTask t where m.taskId = t.id");
		query.append("and m.localKey =", localKey);
		return (Collection<LocalRemoteKey>) dao.queryForList(query);
	}

	public void delete(int id) {
		dao.delete(LocalRemoteKey.class, id);
	}

	public int deleteByLocalKey(String localKey) {
		PrepareQuery query = new PrepareQuery("delete from LocalRemoteKey");
		query.append("where localKey =", localKey);
		return dao.execute(query);
	}

	@SuppressWarnings("unchecked")
	public Collection<LocalRemoteKey> get(String taskId, String remoteType) {
		PrepareQuery query = new PrepareQuery("from LocalRemoteKey");
		query.append("where taskId =", taskId);
		if (!StringUtil.isNullOrBlank(remoteType))
			query.append("and remoteType =", remoteType);
		return (Collection<LocalRemoteKey>) dao.queryForList(query);
	}

	public void saveKey(LocalRemoteKey key) {
		if (key.getId() == 0)
			dao.create(key);
		else
			dao.save(key);
	}

	public int deleteKeysByRemoteKey(String remoteKey) {
		PrepareQuery query = new PrepareQuery("delete from LocalRemoteKey");
		query.append("where remoteKey = ", remoteKey);
		return dao.execute(query);
	}

	public int deleteKeysByTaskId(String taskId) {
		PrepareQuery query = new PrepareQuery("delete from LocalRemoteKey");
		query.append("where taskId = ", taskId);
		return dao.execute(query);
	}
}
