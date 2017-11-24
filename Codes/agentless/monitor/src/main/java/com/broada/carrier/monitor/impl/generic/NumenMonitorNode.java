package com.broada.carrier.monitor.impl.generic;

import com.broada.carrier.monitor.server.api.entity.MonitorNode;

public class NumenMonitorNode extends MonitorNode {
	private static final long serialVersionUID = 1L;

	public NumenMonitorNode(MonitorNode node) {
		super(node);
	}
	
	public String getIpAddress() {
		return getIp();
	}
}
