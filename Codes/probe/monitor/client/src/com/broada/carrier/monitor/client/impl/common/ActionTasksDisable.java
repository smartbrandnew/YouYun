package com.broada.carrier.monitor.client.impl.common;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;

public class ActionTasksDisable extends ActionTasks {

	@Override
	public String getText() {
		return "停用监测";
	}

	@Override
	protected void execute(MonitorTask task) {
		ServerContext.getTaskService().setTaskEnabled(task.getId(), false);
	}

	@Override
	protected boolean ifReloadCacheAfterExecute() {
		return true;
	}

}
