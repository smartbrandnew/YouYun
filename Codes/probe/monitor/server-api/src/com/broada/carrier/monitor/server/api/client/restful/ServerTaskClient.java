package com.broada.carrier.monitor.server.api.client.restful;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.client.restful.entity.SaveTaskRequest;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.service.ServerTaskService;

public class ServerTaskClient extends BaseTaskClient implements ServerTaskService {

	public ServerTaskClient(String baseServiceUrl) {
		super(baseServiceUrl, "/api/v1/monitor/tasks");
	}

	private static class PageEx extends Page<MonitorTask> {
	}

	@Override
	public Page<MonitorTask> getTasksByProbeId(PageNo pageNo, int probeId, boolean currentDomain) {
		return client.get(PageEx.class, "pageFirst", pageNo.getFirst(), "pageSize", pageNo.getSize(), "probeId",
				probeId, "currentDomain", currentDomain);
	}

	@Override
	public Page<MonitorTask> getTasksByPolicyCode(PageNo pageNo, String policyCode) {
		return client.get(PageEx.class, "pageFirst", pageNo.getFirst(), "pageSize", pageNo.getSize(), "policyCode",
				policyCode);
	}

	@Override
	public MonitorTask[] getTasksByNodeId(String nodeId) {
		return client.get(PageEx.class, "nodeId", nodeId).getRows();
	}

	@Override
	public MonitorTask[] getTasksByNodeIds(String[] nodeIds) {
		return client.post("nodeIds", MonitorTask[].class, nodeIds);
	}

	@Override
	public MonitorTask[] getTasksByResourceId(String resourceId) {
		return client.get(PageEx.class, "resourceId", resourceId).getRows();
	}

	@Override
	public MonitorTask[] getTasksByResourceIds(String[] resourceIds) {
		return client.post("resourceIds", MonitorTask[].class, resourceIds);
	}

	@Override
	public String saveTask(MonitorTask task, MonitorInstance[] insts) {
		return client.post(String.class, new SaveTaskRequest(task, insts));
	}

	@Override
	public String saveTask(MonitorTask task) {
		return client.post(String.class, new SaveTaskRequest(task));
	}

	@Override
	public void commitResults(String probeCode, MonitorResult[] results) {
		client.post("0/results?probeCode=" + probeCode, null, results);
	}

	@Override
	public Page<MonitorTask> getTasks(PageNo pageNo, boolean currentDomain) {
		return client.get(PageEx.class, "pageFirst", pageNo.getFirst(), "pageSize", pageNo.getSize(), "currentDomain",
				currentDomain);
	}

	@Override
	public void setTaskEnabled(String taskId, boolean enabled) {
		if (enabled)
			client.post(taskId + "/enable");
		else
			client.post(taskId + "/disable");
	}

	@Override
	public Page<MonitorTask> getTasks(PageNo pageNo) {
		return client.get(PageEx.class, "pageFirst", pageNo.getFirst(), "pageSize", pageNo.getSize());
	}

	@Override
	public MonitorRecord[] getRecords(String taskIds) {
		return client.post("/records", MonitorRecord[].class, taskIds);
	}

}
