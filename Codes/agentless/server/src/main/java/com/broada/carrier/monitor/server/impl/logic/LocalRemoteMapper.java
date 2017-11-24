package com.broada.carrier.monitor.server.impl.logic;

import java.util.Collection;

import com.broada.carrier.monitor.server.impl.entity.LocalRemoteKey;
import com.broada.carrier.monitor.server.impl.pmdb.map.LocalRemoteKeyContext;

public interface LocalRemoteMapper {

	/**
	 * 正常情况下返回的结果集应该只有一个元素
	 * 
	 * @param localKey
	 * @return
	 */
	Collection<LocalRemoteKey> getKeysByLocalKey(String localKey);

	void deleteKey(int id);

	void deleteKeysByLocalKey(String localKey);

	Collection<LocalRemoteKey> getKeys(String taskId, String remoteType);

	void saveContext(LocalRemoteKeyContext context);

	void saveKey(LocalRemoteKey key);

	void deleteKeysByRemoteKey(String remoteKey);

	void deleteKeysByTaskId(String taskId);

	Collection<LocalRemoteKey> getKeysByTaskId(String taskId);

}
