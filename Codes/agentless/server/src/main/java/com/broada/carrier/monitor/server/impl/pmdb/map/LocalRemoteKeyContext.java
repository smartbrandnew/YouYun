package com.broada.carrier.monitor.server.impl.pmdb.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.server.impl.entity.LocalRemoteKey;

public class LocalRemoteKeyContext {
	private String taskId;
	private String remoteType;
	private Map<String, LocalRemoteKey> deleteKeys;
	private List<LocalRemoteKey> createKeys;
	private List<LocalRemoteKey> updateKeys;

	public String getTaskId() {
		return taskId;
	}
	
	public String getRemoteType() {
		return remoteType;
	}

	public LocalRemoteKeyContext(String taskId, String remoteType, Collection<LocalRemoteKey> keys) {
		this.taskId = taskId;
		this.remoteType = remoteType;
		if (keys != null && keys.size() > 0) {
			deleteKeys = new HashMap<String, LocalRemoteKey>();
			for (LocalRemoteKey key : keys) {
				deleteKeys.put(key.getLocalKey(), key);
			}
		}
	}

	public void save(String localKey, String remoteKey, String remoteType) {
		LocalRemoteKey key = deleteKeys == null ? null : deleteKeys.get(localKey);
		if (key == null)
			getCreateKeys().add(new LocalRemoteKey(taskId, localKey, remoteKey, remoteType, new Date()));
		else {
			if (key.getRemoteKey().equals(remoteKey)) {
				deleteKeys.remove(localKey);
				getUpdateKeys().add(key);
			} else {
				getCreateKeys().add(new LocalRemoteKey(taskId, localKey, remoteKey, remoteType, new Date()));
			}
		}
	}

	public Collection<LocalRemoteKey> getCreateKeys() {
		if (createKeys == null)
			createKeys = new ArrayList<LocalRemoteKey>();
		return createKeys;
	}

	public Collection<LocalRemoteKey> getUpdateKeys() {
		if (updateKeys == null)
			updateKeys = new ArrayList<LocalRemoteKey>();
		return updateKeys;
	}

	public Collection<LocalRemoteKey> getDeleteKeys() {
		if (deleteKeys == null)
			return null;

		if (deleteKeys.isEmpty())
			return null;

		return deleteKeys.values();
	}

	public boolean isExistsDeleteKeys() {
		return deleteKeys == null ? false : deleteKeys.size() > 0;
	}

	public boolean isExistsCreateKeys() {
		return createKeys == null ? false : createKeys.size() > 0;
	}
}
