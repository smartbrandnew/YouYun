package com.broada.carrier.monitor.server.api.client.restful;

import com.broada.carrier.monitor.server.api.service.ServerPolicyService;

public class ServerPolicyClient extends BasePolicyClient implements ServerPolicyService {

	public ServerPolicyClient(String baseServiceUrl) {
		super(baseServiceUrl, "/api/v1/monitor/policies");
	}

}
