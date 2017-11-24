package com.broada.carrier.monitor.probe.api.service;

import java.util.List;

import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.service.BaseTaskService;

public interface ProbeTaskService extends BaseTaskService {
	void saveTask(MonitorTask task, MonitorInstance[] instances,
			MonitorRecord record);

	MonitorTask[] getTasks();

	List<String> getAllTaskIds();

	void delete(String id);
}
