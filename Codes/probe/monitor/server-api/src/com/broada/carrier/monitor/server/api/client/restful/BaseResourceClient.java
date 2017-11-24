package com.broada.carrier.monitor.server.api.client.restful;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.service.BaseResourceService;

public class BaseResourceClient extends BaseServiceClient implements BaseResourceService {
	public BaseResourceClient(String baseServiceUrl, String apiPath) {
		super(baseServiceUrl, apiPath);
	}

	@Override
	public MonitorResource getResource(String resourceId) {
		return client.get(resourceId, MonitorResource.class);
	}

	protected static class PageEx extends Page<MonitorResource> {
	}

	@Override
	public String saveResource(MonitorResource resource) {
		return client.post(String.class, resource);
	}

	@Override
	public OperatorResult deleteResource(String id) {
		return client.post(id + "/delete", OperatorResult.class);
	}

}
