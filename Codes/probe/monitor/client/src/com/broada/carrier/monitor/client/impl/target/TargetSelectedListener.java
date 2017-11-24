package com.broada.carrier.monitor.client.impl.target;

import com.broada.carrier.monitor.server.api.entity.MonitorTarget;

public interface TargetSelectedListener {
	void onSelected(MonitorTarget target);
}
