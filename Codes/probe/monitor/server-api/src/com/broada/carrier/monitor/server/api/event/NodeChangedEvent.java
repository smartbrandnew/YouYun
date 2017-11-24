package com.broada.carrier.monitor.server.api.event;

import com.broada.carrier.monitor.server.api.entity.MonitorNode;

public class NodeChangedEvent extends ObjectChangedEvent<MonitorNode> {
	private static final long serialVersionUID = 1L;

	public NodeChangedEvent(ObjectChangedType type, MonitorNode oldObject, MonitorNode newObject) {
		super(type, oldObject, newObject);		
	}
}
