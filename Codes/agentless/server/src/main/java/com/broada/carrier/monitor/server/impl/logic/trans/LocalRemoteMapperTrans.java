package com.broada.carrier.monitor.server.impl.logic.trans;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.server.impl.dao.LocalRemoteKeyDao;
import com.broada.carrier.monitor.server.impl.entity.LocalRemoteKey;
import com.broada.carrier.monitor.server.impl.logic.LocalRemoteMapper;
import com.broada.carrier.monitor.server.impl.pmdb.map.LocalRemoteKeyContext;

public class LocalRemoteMapperTrans implements LocalRemoteMapper {
	private static final Logger logger = LoggerFactory.getLogger(LocalRemoteMapperTrans.class);
	@Autowired
	private LocalRemoteKeyDao dao;

	@Override
	public Collection<LocalRemoteKey> getKeysByLocalKey(String localKey) {
		return dao.getByLocalKey(localKey);
	}
	
	@Override
	public void deleteKey(int id) {
		logger.debug("删除映射，按id：{}", id);
		dao.delete(id);
	}

	@Override
	public void deleteKeysByLocalKey(String localKey) {
		logger.debug("删除映射，按localKey：{}", localKey);
		dao.deleteByLocalKey(localKey);
	}

	@Override
	public Collection<LocalRemoteKey> getKeys(String taskId, String remoteType) {
		return dao.get(taskId, remoteType);
	}

	@Override
	public void saveContext(LocalRemoteKeyContext context) {
		if (context.isExistsDeleteKeys()) {
			for (LocalRemoteKey key : context.getDeleteKeys())
				deleteKey(key.getId());
		}
		if (context.isExistsCreateKeys()) {
			for (LocalRemoteKey key : context.getCreateKeys())
				saveKey(key);
		}
	}

	@Override
	public void saveKey(LocalRemoteKey key) {
		logger.debug("保存映射：{}", key);		
		dao.saveKey(key);
	}

	@Override
	public void deleteKeysByRemoteKey(String remoteKey) {
		logger.debug("删除映射，按remoteKey：{}", remoteKey);
		dao.deleteKeysByRemoteKey(remoteKey);
	}

	@Override
	public void deleteKeysByTaskId(String taskId) {
		dao.deleteKeysByTaskId(taskId);
	}

	@Override
	public Collection<LocalRemoteKey> getKeysByTaskId(String taskId) {
		return dao.get(taskId, null);
	}	
}
