package com.broada.carrier.monitor.server.api.service;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public interface ServerMethodService extends BaseMethodService {
	MonitorMethod[] getMethodsByTypeId(String typeId);
	
	void createMethod(MonitorMethod method);
	
	MonitorMethod[] getMethodsByNodeIdAndType(String nodeId,String typeId);
}
