package com.broada.carrier.monitor.server.api.service;

import com.broada.carrier.monitor.server.api.entity.CollectParams;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.CollectTaskSign;
import com.broada.carrier.monitor.server.api.entity.ExecuteParams;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.TestParams;

/**
 * 监测任务管理服务
 * 
 * @author Jiangjw
 */
public interface BaseTaskService {
	MonitorTask getTask(String taskId);

	MonitorInstance[] getInstancesByTaskId(String taskId);

	void deleteTask(String taskId);

	void dispatchTask(String taskId);

	MonitorResult testTask(TestParams params);

	MonitorResult executeTask(String taskId, ExecuteParams params);

	Object collectTask(CollectParams params);

	MonitorRecord getRecord(String taskId);

	void cancelCollect(String nodeId, String taskId);

	CollectResult getCollectResult(String nodeId, String taskId);

	CollectTaskSign commitTask(CollectParams params);

}
