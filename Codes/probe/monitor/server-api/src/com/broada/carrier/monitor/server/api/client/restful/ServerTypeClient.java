package com.broada.carrier.monitor.server.api.client.restful;

import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.server.api.service.ServerTypeService;

public class ServerTypeClient extends BaseTypeClient implements ServerTypeService {
	public ServerTypeClient(String baseServiceUrl) {		
		super(baseServiceUrl, "/api/v1/monitor");
	}
	
	@Override
	public MonitorType[] getTypesByTargetTypeId(String targetTypeId) {
		return client.get("types", MonitorType[].class, "targetTypeId", targetTypeId);
	}
}
