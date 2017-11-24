package com.broada.carrier.monitor.server.impl.pmdb.cache;

import java.io.Serializable;

import com.broada.cmdb.api.data.Instance;

public class CIEntry implements Serializable {
	private static final long serialVersionUID = 1L;
	private String localKey;
	private String remoteClassCode;
	private String remoteKey;
	private Instance instance;

	public CIEntry(String localKey, String remoteClassCode, String remoteKey, Instance instance) {
		this.localKey = localKey;
		this.remoteClassCode = remoteClassCode;
		this.remoteKey = remoteKey;
		this.instance = instance;
	}

	public String getLocalKey() {
		return localKey;
	}

	public String getRemoteClassCode() {
		return remoteClassCode;
	}

	public String getRemoteKey() {
		return remoteKey;
	}

	public Instance getInstance() {
		return instance;
	}

}
