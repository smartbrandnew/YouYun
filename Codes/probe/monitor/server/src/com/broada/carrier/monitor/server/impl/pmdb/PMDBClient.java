package com.broada.carrier.monitor.server.impl.pmdb;

import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.entity.MonitorTarget;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetGroup;

public interface PMDBClient {
	MonitorTargetGroup[] getGroups(String userId, String domainId, String parentId);
	
	MonitorTarget[] getGroupInstances(PageNo pageNo, String groupId, String ipKey);
}
