package com.broada.carrier.monitor.server.api.client.restful;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.service.BaseMethodService;

public class BaseMethodClient extends BaseServiceClient implements BaseMethodService {
	public BaseMethodClient(String baseServiceUrl, String apiPath) {
		super(baseServiceUrl, apiPath);
	}

	@Override
	public MonitorMethod getMethod(String methodCode) {
		if (methodCode == null)
			throw new IllegalArgumentException();
		return client.get(methodCode, MonitorMethod.class);
	}

	@Override
	public MonitorMethod[] getMethods() {
		return client.get(MonitorMethod[].class);
	}

	@Override
	public void saveMethod(MonitorMethod method) {
		client.post(null, method);
	}

	@Override
	public OperatorResult deleteMethod(String methodCode) {
		return client.post(methodCode + "/delete", OperatorResult.class);
	}
}
