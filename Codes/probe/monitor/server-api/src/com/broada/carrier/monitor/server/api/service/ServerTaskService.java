package com.broada.carrier.monitor.server.api.service;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;

/**
 * 监测任务管理服务
 * 
 * @author Jiangjw
 */
public interface ServerTaskService extends BaseTaskService {
	Page<MonitorTask> getTasks(PageNo pageNo, boolean currentDomain);

	Page<MonitorTask> getTasks(PageNo pageNo);

	Page<MonitorTask> getTasksByProbeId(PageNo pageNo, int probeId, boolean currentDomain);

	MonitorTask[] getTasksByNodeId(String nodeId);

	MonitorTask[] getTasksByNodeIds(String[] nodeIds);

	MonitorTask[] getTasksByResourceId(String resourceId);

	MonitorTask[] getTasksByResourceIds(String[] resourceIds);

	String saveTask(MonitorTask task);

	String saveTask(MonitorTask task, MonitorInstance[] instances);

	void commitResults(String probeCode, MonitorResult[] results);

	void setTaskEnabled(String taskId, boolean enabled);

	Page<MonitorTask> getTasksByPolicyCode(PageNo pageNo, String policyCode);

	/**
	 * 批量查询监测任务的运行记录
	 * 
	 * @param taskIds
	 * @return
	 */
	MonitorRecord[] getRecords(String taskIds);
}
