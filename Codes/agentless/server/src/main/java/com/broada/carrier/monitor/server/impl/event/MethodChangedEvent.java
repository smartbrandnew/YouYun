package com.broada.carrier.monitor.server.impl.event;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.event.ObjectChangedEvent;
import com.broada.carrier.monitor.server.api.event.ObjectChangedType;

public class MethodChangedEvent extends ObjectChangedEvent<MonitorMethod> {
	private static final long serialVersionUID = 1L;

	public MethodChangedEvent(ObjectChangedType type, MonitorMethod oldObject, MonitorMethod newObject) {
		super(type, oldObject, newObject);		
	}
}
