package com.broada.carrier.monitor.server.api.client.restful;

import com.broada.carrier.monitor.common.util.Base64Util;
import com.broada.carrier.monitor.server.api.entity.CollectParams;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.CollectTaskSign;
import com.broada.carrier.monitor.server.api.entity.ExecuteParams;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.TestParams;
import com.broada.carrier.monitor.server.api.service.BaseTaskService;

public class BaseTaskClient extends BaseServiceClient implements BaseTaskService {

	public BaseTaskClient(String baseServiceUrl, String apiPath) {
		super(baseServiceUrl, apiPath);
	}

	@Override
	public void deleteTask(String taskId) {
		client.post(taskId + "/delete");
	}

	@Override
	public MonitorResult executeTask(String taskId, ExecuteParams params) {
		return client.post(taskId + "/execute", MonitorResult.class, params);
	}

	@Override
	public MonitorTask getTask(String taskId) {
		return client.get(taskId, MonitorTask.class);
	}

	@Override
	public Object collectTask(CollectParams params) {
		String data = client.post("0/collect", String.class, params);
		return Base64Util.decodeObject(data);
	}

	@Override
	public MonitorRecord getRecord(String taskId) {
		return client.get(taskId + "/record", MonitorRecord.class);
	}

	@Override
	public MonitorInstance[] getInstancesByTaskId(String taskId) {
		return client.get(taskId + "/instances", MonitorInstance[].class);
	}

	@Override
	public void dispatchTask(String taskId) {
		client.post(taskId + "/dispatch");
	}

	@Override
	public MonitorResult testTask(TestParams params) {
		return client.post("0/test", MonitorResult.class, params);
	}

	@Override
	public void cancelCollect(String nodeId, String taskId) {
		client.post(nodeId + "/" + taskId + "/cancelCollect");

	}

	@Override
	public CollectResult getCollectResult(String nodeId, String taskId) {
		return client.post(nodeId + "/" + taskId + "/getCollectResult", CollectResult.class);

	}

	@Override
	public CollectTaskSign commitTask(CollectParams params) {
		return client.post("0/commit", CollectTaskSign.class, params);
	}
	
}
