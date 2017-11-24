package com.broada.carrier.monitor.probe.impl.entity;

import com.broada.carrier.monitor.probe.impl.logic.ProbeTaskServiceEx;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.TestParams;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.carrier.monitor.spi.entity.MonitorTempData;

public class ProbeSideMonitorContext extends MonitorContext {
	private ProbeTaskServiceEx taskService;

	public ProbeSideMonitorContext(MonitorNode node, MonitorResource resource, MonitorMethod method, MonitorTask task,
			MonitorInstance[] instances, ProbeTaskServiceEx taskService, MonitorPolicy policy, MonitorRecord record) {
		super(node, resource, method, task, instances, policy, record);
		this.taskService = taskService;
	}

	public ProbeSideMonitorContext(TestParams copy, ProbeTaskServiceEx taskService) {
		super(copy);
		this.taskService = taskService;
	}

	public ProbeSideMonitorContext(MonitorContext copy, ProbeTaskServiceEx taskService) {
		super(copy);
		this.taskService = taskService;
	}

	@Override
	public MonitorTempData getTempData() {
		if (getTask().getId() == null || getTask().getId().trim().length() <= 0)
			return null;
		return taskService.getTempData(getTask().getId());
	}

	@Override
	public void setTempData(MonitorTempData data) {
		if (getTask().getId() == null || getTask().getId().trim().length() <= 0)
			return;
		data.setTaskId(getTask().getId());
		taskService.saveTempData(data);
	}

}
