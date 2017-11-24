package com.broada.carrier.monitor.probe.api.client.restful;

import com.broada.carrier.monitor.probe.api.service.ProbePolicyService;
import com.broada.carrier.monitor.server.api.client.restful.BasePolicyClient;

public class ProbePolicyClient extends BasePolicyClient implements ProbePolicyService {

	public ProbePolicyClient(String baseServiceUrl) {
		super(baseServiceUrl, "/api/v1/probe/policies");
	}
}
