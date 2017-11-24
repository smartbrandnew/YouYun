package com.broada.carrier.monitor.server.api.event;

import java.io.Serializable;

public interface SystemEvent extends Serializable {
	public static final String TOPIC = "monitor.system.event";
	
	String getType();
}
