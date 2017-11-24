package com.broada.carrier.monitor.server.api.event;

import com.broada.carrier.monitor.server.api.entity.MonitorProbe;

public class ProbeChangedEvent extends ObjectChangedEvent<MonitorProbe> {
	private static final long serialVersionUID = 1L;

	public ProbeChangedEvent(ObjectChangedType type, MonitorProbe oldObject, MonitorProbe newObject) {
		super(type, oldObject, newObject);		
	}
}
