package com.broada.carrier.monitor.server.impl.logic;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.server.api.entity.MonitorTargetGroup;
import com.broada.carrier.monitor.server.api.service.ServerTargetGroupService;
import com.broada.carrier.monitor.server.impl.pmdb.PMDBFacade;

public class ServerTargetGroupServiceImpl implements ServerTargetGroupService {
	@Autowired	
	private PMDBFacade pmdbFacade;

	@Override
	public MonitorTargetGroup[] getGroupsByParentId(String parentId) {
		String userId = SessionManager.checkSessionUserId();
		String domainId = SessionManager.checkSessionDomainId();		
		return pmdbFacade.getGroups(userId, domainId, parentId);
	}
}
