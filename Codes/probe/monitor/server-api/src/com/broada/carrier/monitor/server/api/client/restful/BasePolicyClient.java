package com.broada.carrier.monitor.server.api.client.restful;

import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.service.BasePolicyService;

public class BasePolicyClient extends BaseServiceClient implements BasePolicyService {
	public BasePolicyClient(String baseServiceUrl, String apiPath) {
		super(baseServiceUrl, apiPath);
	}

	@Override
	public MonitorPolicy getPolicy(String policyCode) {
		return client.get(policyCode, MonitorPolicy.class);
	}

	@Override
	public MonitorPolicy[] getPolicies() {
		return client.get(MonitorPolicy[].class);
	}

	@Override
	public void savePolicy(MonitorPolicy policy) {
		client.post(null, policy);
	}

	@Override
	public OperatorResult deletePolicy(String policyCode) {
		if (policyCode == null || policyCode.isEmpty())
			throw new IllegalArgumentException();
		return client.post(policyCode + "/delete", OperatorResult.class);
	}

}
