package com.broada.carrier.monitor.server.api.service;

import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;

public interface BasePolicyService {
	
	MonitorPolicy getPolicy(String policyCode);

	MonitorPolicy[] getPolicies();

	void savePolicy(MonitorPolicy  policy);
	
	OperatorResult deletePolicy(String policyCode);
}
