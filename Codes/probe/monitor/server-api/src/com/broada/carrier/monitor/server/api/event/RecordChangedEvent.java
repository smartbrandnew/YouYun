package com.broada.carrier.monitor.server.api.event;

import com.broada.carrier.monitor.server.api.entity.MonitorRecord;

public class RecordChangedEvent extends ObjectChangedEvent<MonitorRecord> {
	private static final long serialVersionUID = 1L;

	public RecordChangedEvent(ObjectChangedType type, MonitorRecord oldObject, MonitorRecord newObject) {
		super(type, oldObject, newObject);		
	}
}
