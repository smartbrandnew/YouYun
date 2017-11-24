package com.broada.carrier.monitor.probe.impl.logic;


import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.probe.api.service.ProbeTaskService;
import com.broada.carrier.monitor.probe.impl.entity.MonitorResultCache;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.spi.entity.MonitorTempData;

public interface ProbeTaskServiceEx extends ProbeTaskService {
	MonitorRecord getRecord(String taskId);
	
	void saveRecord(MonitorRecord record);

	MonitorTask[] getTasks();

	void deleteAll();

	MonitorTask[] getTasksByPolicyCode(String policyCode);

	int getResultCachesCount();

	MonitorResultCache[] getResultCaches(PageNo pageNo);

	void deleteResultCaches(MonitorResultCache[] caches);

	void saveResultCache(MonitorResultCache cache);

	MonitorInstance[] getInstancesByTaskId(String taskId);
	
	void saveTempData(MonitorTempData tempData);
	
	MonitorTempData getTempData(String taskId);
	
	
}
