package com.broada.carrier.monitor.server.api.event;

import com.broada.carrier.monitor.server.api.entity.MonitorTask;

public class TaskChangedEvent extends ObjectChangedEvent<MonitorTask> {
	private static final long serialVersionUID = 1L;

	public TaskChangedEvent(ObjectChangedType type, MonitorTask oldObject, MonitorTask newObject) {
		super(type, oldObject, newObject);		
	}
}
