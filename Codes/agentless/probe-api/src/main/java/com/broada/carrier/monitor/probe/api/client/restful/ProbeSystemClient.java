package com.broada.carrier.monitor.probe.api.client.restful;

import com.broada.carrier.monitor.probe.api.service.ProbeSystemService;
import com.broada.carrier.monitor.server.api.client.restful.BaseSystemClient;

public class ProbeSystemClient extends BaseSystemClient implements ProbeSystemService {
	public ProbeSystemClient(String baseServiceUrl) {		
		super(baseServiceUrl, "/api/v1/probe/system");
	}	

	@Override
	public void deleteAll() {
		client.post("deleteAll");
	}

	@Override
	public void exit(String reason) {
		client.post("exit", null, reason);
	}
}
