package com.broada.carrier.monitor.server.api.client.restful;

import com.broada.carrier.monitor.server.api.service.ServerFileService;

public class ServerFileClient extends BaseFileClient implements ServerFileService {

	public ServerFileClient(String baseServiceUrl) {
		super(baseServiceUrl, "/api/v1/monitor/files");
	}
}
