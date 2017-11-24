package com.broada.carrier.monitor.server.api.event;

import com.broada.carrier.monitor.server.api.entity.MonitorTargetStatus;

public class TargetStatusChangedEvent extends ObjectChangedEvent<MonitorTargetStatus> {
	private static final long serialVersionUID = 1L;

	public TargetStatusChangedEvent(ObjectChangedType type, MonitorTargetStatus oldObject, MonitorTargetStatus newObject) {
		super(type, oldObject, newObject);		
	}
}
