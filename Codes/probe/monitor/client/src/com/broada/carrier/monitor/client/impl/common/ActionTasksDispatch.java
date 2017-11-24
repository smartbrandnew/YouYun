package com.broada.carrier.monitor.client.impl.common;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;

public class ActionTasksDispatch extends ActionTasks {

	@Override
	public String getText() {
		return "立即监测";
	}

	@Override
	protected void execute(MonitorTask task) {
		ServerContext.getTaskService().dispatchTask(task.getId());
	}

	@Override
	protected boolean ifReloadCacheAfterExecute() {
		return false;
	}

}
