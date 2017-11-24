package com.broada.carrier.monitor.server.api.client;

import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.carrier.monitor.server.api.service.BaseNodeService;
import com.broada.carrier.monitor.server.api.service.ServerProbeService;
import com.broada.carrier.monitor.server.api.service.ServerTargetTypeService;

public class ServerUtil extends BaseServiceUtil {

	public static MonitorTargetType checkTargetType(ServerTargetTypeService targetTypeService, String typeId) {
		MonitorTargetType targetType = targetTypeService.getTargetType(typeId);
		if (targetType == null)
			throw new IllegalArgumentException("监测项类型不存在：" + typeId);
		return targetType;
	}

	public static MonitorProbe checkProbe(ServerProbeService probeService, BaseNodeService nodeService, String nodeId) {
		MonitorNode node = checkNode(nodeService, nodeId);
		return checkProbe(probeService, node.getProbeId());
	}

	public static MonitorProbe checkProbe(ServerProbeService probeService, int probeId) {
		MonitorProbe probe = probeService.getProbe(probeId);
		if (probe == null)
			throw new IllegalArgumentException("监测探针不存在：" + probeId);
		return probe;
	}

}
