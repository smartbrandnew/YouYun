package com.broada.carrier.monitor.server.impl.logic;

import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.service.ServerTaskService;

public interface ServerTaskServiceEx extends ServerTaskService {
	MonitorRecord commitResult(MonitorResult result);

	int getTasksCountByState(MonitorState state);

	int getTasksCountByProcessed();

	double getTasksSpeedByProcessed();

	void deleteTaskByNodeId(String nodeId);
	
	void deleteTaskByResourceId(String resourceId);

	String[] getMethodCodesByNodeId(String nodeId);

	String[] getMethodCodesByResourceId(String resourceId);
	
	String[] getTaskNodeIds();
	
	String[] getTaskResourceIds();
	
	int getLicenseUsedQuotaPCServer();
	int getLicenseUsedQuotaMiniServer();
	int getLicenseUsedQuotaStorageDev();
	int getLicenseUsedQuotaAppPlatform();
}
