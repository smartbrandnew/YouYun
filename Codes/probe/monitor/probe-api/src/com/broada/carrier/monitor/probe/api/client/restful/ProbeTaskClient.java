package com.broada.carrier.monitor.probe.api.client.restful;

import java.util.List;

import com.broada.carrier.monitor.probe.api.client.restful.entity.SaveTaskRequest;
import com.broada.carrier.monitor.probe.api.service.ProbeTaskService;
import com.broada.carrier.monitor.server.api.client.restful.BaseTaskClient;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;

public class ProbeTaskClient extends BaseTaskClient implements ProbeTaskService {
	public ProbeTaskClient(String baseServiceUrl) {		
		super(baseServiceUrl, "/api/v1/probe/tasks");
	}
	
	@Override
	public void saveTask(MonitorTask task, MonitorInstance[] instances, MonitorRecord record) {
		client.post(null, new SaveTaskRequest(task, instances, record));
	}

	@Override
	public MonitorTask[] getTasks() {
		return client.get(MonitorTask[].class);
	}

	@Override
	public List<String> getAllTaskIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(String id) {
		// TODO Auto-generated method stub
		
	}
}
