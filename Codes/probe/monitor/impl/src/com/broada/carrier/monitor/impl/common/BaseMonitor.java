package com.broada.carrier.monitor.impl.common;

import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

public abstract class BaseMonitor implements Monitor {
	@Override
	public MonitorResult monitor(MonitorContext context) {
		return (MonitorResult) collect(new CollectContext(context));
	}
}
