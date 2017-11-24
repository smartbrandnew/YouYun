package com.broada.carrier.monitor.server.impl.logic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.broada.carrier.monitor.server.impl.entity.LocalRemoteKey;
import com.broada.carrier.monitor.server.impl.pmdb.map.LocalRemoteKeyContext;

public class LocalRemoteMapperImpl implements LocalRemoteMapper {
	private LocalRemoteMapper target;
	private Map<String, Map<String, LocalRemoteKey>> cacheByTaskIdRemoteType = new ConcurrentHashMap<String, Map<String, LocalRemoteKey>>();
	private Map<String, Collection<LocalRemoteKey>> cacheByLocalKey = new ConcurrentHashMap<String, Collection<LocalRemoteKey>>();

	public LocalRemoteMapperImpl(LocalRemoteMapper target) {		
		this.target = target;
	}

	@Override
	public Collection<LocalRemoteKey> getKeysByLocalKey(String localKey) {		
		Collection<LocalRemoteKey> value = cacheByLocalKey.get(localKey);
		if (value == null) {
			value = target.getKeysByLocalKey(localKey);
			cacheByLocalKey.put(localKey, value);
		}
		return value;
	}
	
	private static String getCacheKey(String taskId, String remoteType) {
		return taskId + "." + remoteType;
	}
	
	private Map<String, LocalRemoteKey> getKeysMapByTaskId(String taskId, String remoteType) {
		String cacheKey = getCacheKey(taskId, remoteType);
		Map<String, LocalRemoteKey> keys = cacheByTaskIdRemoteType.get(cacheKey);
		if (keys == null) {
			Collection<LocalRemoteKey> items = target.getKeys(taskId, remoteType);
			keys = new HashMap<String, LocalRemoteKey>(); 
			if (items != null) {
				for (LocalRemoteKey key : items)
					keys.put(key.getLocalKey(), key);
			}
			cacheByTaskIdRemoteType.put(cacheKey, keys);
		}
		return keys;
	}
	
	@Override
	public void deleteKey(int id) {
		target.deleteKey(id);		
	}

	@Override
	public void deleteKeysByLocalKey(String localKey) {
		target.deleteKeysByLocalKey(localKey);
		cacheByTaskIdRemoteType.clear();
		cacheByLocalKey.remove(localKey);
	}

	@Override
	public Collection<LocalRemoteKey> getKeys(String taskId, String remoteType) {
		return getKeysMapByTaskId(taskId, remoteType).values();
	}
	
	@Override
	public void saveContext(LocalRemoteKeyContext context) {		
		target.saveContext(context);
		
		Map<String, LocalRemoteKey> keys = getKeysMapByTaskId(context.getTaskId(), context.getRemoteType());
		if (context.isExistsDeleteKeys()) {
			for (LocalRemoteKey key : context.getDeleteKeys()) {
				keys.remove(key.getLocalKey());
			}
		}
		if (context.isExistsCreateKeys()) {
			for (LocalRemoteKey key : context.getCreateKeys()) {
				keys.put(key.getLocalKey(), key);
			}
		}		
	}

	@Override
	public void saveKey(LocalRemoteKey key) {
		target.saveKey(key);
		cacheByLocalKey.remove(key.getLocalKey());
	}

	@Override
	public void deleteKeysByRemoteKey(String remoteKey) {
		target.deleteKeysByRemoteKey(remoteKey);
		cacheByTaskIdRemoteType.clear();
		cacheByLocalKey.clear();
	}

	@Override
	public void deleteKeysByTaskId(String taskId) {
		target.deleteKeysByTaskId(taskId);
		cacheByTaskIdRemoteType.clear();
		cacheByLocalKey.clear();
	}

	@Override
	public Collection<LocalRemoteKey> getKeysByTaskId(String taskId) {
		return target.getKeysByTaskId(taskId);
	}
}
