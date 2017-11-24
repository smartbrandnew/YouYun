package com.broada.carrier.monitor.server.api.service;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;

public interface BaseMethodService {
	MonitorMethod[] getMethods();

	MonitorMethod getMethod(String methodCode);

	void saveMethod(MonitorMethod method);

	OperatorResult deleteMethod(String methodCode);
}
