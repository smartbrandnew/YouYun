package com.broada.carrier.monitor.server.api.client.restful;

import com.broada.carrier.monitor.common.restful.BaseClient;

public class BaseServiceClient {
	protected BaseClient client;

	public BaseServiceClient(String baseServiceUrl, String apiPath) {
		client = new BaseClient(baseServiceUrl, apiPath, apiPath.contains("probe") ? "探针" : "服务端");
	}

	public BaseClient getClient() {
		return client;
	} 	
}
