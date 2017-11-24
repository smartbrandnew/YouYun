package com.broada.carrier.monitor.impl.common;

import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.carrier.monitor.spi.entity.MonitorTempData;

public class TestMonitorContext extends MonitorContext {
	private MonitorTempData tempData;
	
	public TestMonitorContext(MonitorNode node, MonitorResource resource, MonitorMethod method, MonitorTask task,
			MonitorInstance[] instances) {
		super(node, resource, method, task, instances, null, null);		
	}

	@Override
	public void setTempData(MonitorTempData data) {
		this.tempData = data;		
	}

	@Override
	public MonitorTempData getTempData() {
		return tempData;
	}
}
