package com.broada.carrier.monitor.probe.api.client.restful;

import com.broada.carrier.monitor.probe.api.service.ProbeFileService;
import com.broada.carrier.monitor.server.api.client.restful.BaseFileClient;

public class ProbeFileClient extends BaseFileClient implements ProbeFileService {

	public ProbeFileClient(String baseServiceUrl) {
		super(baseServiceUrl, "/api/v1/probe/files");
	}
}
