package com.broada.carrier.monitor.server.api.client.restful;

import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetStatus;
import com.broada.carrier.monitor.server.api.service.ServerNodeService;

public class ServerNodeClient extends BaseNodeClient implements ServerNodeService {
	public ServerNodeClient(String baseServiceUrl) {		
		super(baseServiceUrl, "/api/v1/monitor/nodes");
	}
	
	private static class PageEx extends Page<MonitorNode> {		
	}	

	@Override
	public Page<MonitorNode> getNodesByGroupId(PageNo pageNo, String groupId) {
		return client.get(PageEx.class, "pageFirst", pageNo.getFirst(), "pageSize", pageNo.getSize(), "groupId", groupId);
	}

	@Override
	public Page<MonitorNode> getNodesByProbeId(PageNo pageNo, int probeId, boolean currentDomain) {
		return client.get(PageEx.class, "pageFirst", pageNo.getFirst(), "pageSize", pageNo.getSize(), "probeId", probeId, "currentDomain", currentDomain);		
	}

	@Override
	public int getNodeProbeId(String nodeId) {
		return client.get(nodeId + "/probeId", Integer.class);
	}
	
	@Override
	public Map<String, Integer> getNodeProbeId(String[] nodeIds) {
		throw new UnsupportedOperationException("不支持的查询");
	}

	@Override
	public Page<MonitorNode> getNodesByIp(PageNo pageNo, String ip) {
		return client.get(PageEx.class, "pageFirst", pageNo.getFirst(), "pageSize", pageNo.getSize(), "ip", ip);
	}

	@Override
	public MonitorTargetStatus getNodeStatus(String nodeId) {
		return client.get(nodeId + "/status", MonitorTargetStatus.class);
	}
	
	@Override
	public MonitorTargetStatus[] getNodesStatus(String[] nodeIds) {
		return client.post("nodesStatus", MonitorTargetStatus[].class, nodeIds);
	}

	@Override
	public Page<MonitorNode> getNodes(boolean currentDomain) {
		return client.get(PageEx.class, "currentDomain", currentDomain);
	}

	@Override
	public MonitorNode[] getNodes(List<String> ids, boolean currentDomain) {
		return client.post("ids?currentDomain=" + currentDomain, MonitorNode[].class, ids);
	}

}
