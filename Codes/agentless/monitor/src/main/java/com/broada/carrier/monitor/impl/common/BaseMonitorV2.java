package com.broada.carrier.monitor.impl.common;

import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.spi.MonitorV2;
import com.broada.carrier.monitor.spi.entity.CollectContextV2;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

public abstract class BaseMonitorV2 implements MonitorV2 {
	@Override
	public MonitorResult monitor(MonitorContext context) {
		return (MonitorResult) collect(new CollectContextV2(context));
	}
}
