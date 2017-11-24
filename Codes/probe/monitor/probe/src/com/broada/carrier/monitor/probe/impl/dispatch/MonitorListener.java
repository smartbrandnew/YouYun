package com.broada.carrier.monitor.probe.impl.dispatch;

import com.broada.carrier.monitor.server.api.entity.MonitorResult;

public interface MonitorListener {
	void onComplete(MonitorResult result);

	void onError(RuntimeException error);
}
