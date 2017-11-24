package com.broada.carrier.monitor.server.impl.event;

import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.event.ObjectChangedEvent;
import com.broada.carrier.monitor.server.api.event.ObjectChangedType;

public class PolicyChangedEvent extends ObjectChangedEvent<MonitorPolicy> {
	private static final long serialVersionUID = 1L;

	public PolicyChangedEvent(ObjectChangedType type, MonitorPolicy oldObject, MonitorPolicy newObject) {
		super(type, oldObject, newObject);		
	}
}
