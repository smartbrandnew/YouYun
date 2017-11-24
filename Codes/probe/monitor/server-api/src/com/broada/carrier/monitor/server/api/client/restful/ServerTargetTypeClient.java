package com.broada.carrier.monitor.server.api.client.restful;

import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.carrier.monitor.server.api.service.ServerTargetTypeService;

public class ServerTargetTypeClient extends BaseServiceClient implements ServerTargetTypeService {
	public ServerTargetTypeClient(String baseServiceUrl) {		
		super(baseServiceUrl, "/api/v1/monitor/targetTypes");
	}

	@Override
	public MonitorTargetType[] getTargetTypesByNode() {
		return client.get(MonitorTargetType[].class, "type", "node");
	}

	@Override
	public MonitorTargetType[] getTargetTypesByResource() {		
		return client.get(MonitorTargetType[].class, "type", "resource");		
	}

	@Override
	public MonitorTargetType[] getTargetTypesByParentId(String parentId) {
		return client.get(MonitorTargetType[].class, "parentId", parentId);		
	}

	@Override
	public MonitorTargetType getTargetType(String typeId) {
		return client.get(typeId, MonitorTargetType.class);
	}
	
}
