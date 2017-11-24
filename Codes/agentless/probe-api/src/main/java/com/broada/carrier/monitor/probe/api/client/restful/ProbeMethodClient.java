package com.broada.carrier.monitor.probe.api.client.restful;

import com.broada.carrier.monitor.probe.api.service.ProbeMethodService;
import com.broada.carrier.monitor.server.api.client.restful.BaseMethodClient;

public class ProbeMethodClient extends BaseMethodClient implements ProbeMethodService {

	public ProbeMethodClient(String baseServiceUrl) {
		super(baseServiceUrl, "/api/v1/probe/methods");
	}

}
