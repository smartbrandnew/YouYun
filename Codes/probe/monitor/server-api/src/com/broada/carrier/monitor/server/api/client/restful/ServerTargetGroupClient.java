package com.broada.carrier.monitor.server.api.client.restful;

import com.broada.carrier.monitor.server.api.entity.MonitorTargetGroup;
import com.broada.carrier.monitor.server.api.service.ServerTargetGroupService;

public class ServerTargetGroupClient extends BaseServiceClient implements ServerTargetGroupService {
	public ServerTargetGroupClient(String baseServiceUrl) {		
		super(baseServiceUrl, "/api/v1/monitor/targetGroups");
	}

	@Override
	public MonitorTargetGroup[] getGroupsByParentId(String parentId) {
		return client.get(MonitorTargetGroup[].class, "parentId", parentId);			
	}

}
