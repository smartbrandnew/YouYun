package com.broada.carrier.monitor.server.api.event;

import com.broada.carrier.monitor.server.api.entity.MonitorResource;

public class ResourceChangedEvent extends ObjectChangedEvent<MonitorResource> {
	private static final long serialVersionUID = 1L;

	public ResourceChangedEvent(ObjectChangedType type, MonitorResource oldObject, MonitorResource newObject) {
		super(type, oldObject, newObject);		
	}
}
