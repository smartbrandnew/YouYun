package com.broada.carrier.monitor.probe.api.client.restful;

import com.broada.carrier.monitor.probe.api.service.ProbeResourceService;
import com.broada.carrier.monitor.server.api.client.restful.BaseResourceClient;

public class ProbeResourceClient extends BaseResourceClient implements ProbeResourceService {
	public ProbeResourceClient(String baseServiceUrl) {		
		super(baseServiceUrl, "/api/v1/probe/resources");
	}
}
