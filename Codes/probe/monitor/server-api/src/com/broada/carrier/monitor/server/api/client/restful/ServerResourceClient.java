package com.broada.carrier.monitor.server.api.client.restful;

import java.util.Map;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetStatus;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.service.ServerResourceService;

public class ServerResourceClient extends BaseResourceClient implements ServerResourceService {
	public ServerResourceClient(String baseServiceUrl) {
		super(baseServiceUrl, "/api/v1/monitor/resources");
	}

	@Override
	public Page<MonitorResource> getResourcesByGroupId(PageNo pageNo, String groupId) {
		return client.get(PageEx.class, "pageFirst", pageNo.getFirst(), "pageSize", pageNo.getSize(), "groupId", groupId);
	}

	@Override
	public MonitorResource[] getResourcesByNodeId(String nodeId) {
		return client.get(PageEx.class, "nodeId", nodeId).getRows();
	}

	@Override
	public String saveResource(MonitorResource resource) {
		return client.post(String.class, resource);
	}

	@Override
	public OperatorResult deleteResource(String id) {
		return client.post(id + "/delete", OperatorResult.class);
	}

	@Override
	public String getResourceNodeId(String resourceId) {
		return client.get(resourceId + "/nodeId", String.class);
	}

	@Override
	public MonitorTargetStatus getResourceStatus(String resourceId) {
		return client.get(resourceId + "/status", MonitorTargetStatus.class);
	}

	@Override
	public Page<MonitorResource> getResourcesByNodeIds(String ids) {
		return client.post("ids", PageEx.class, ids);
	}

	@Override
	public Map<String, String> getResourceNodeId(String[] resourceIds) {
		throw new UnsupportedOperationException("不支持客户端查询资源的节点id");
	}

	@Override
	public MonitorTargetStatus[] getResourcesStatus(String[] resourceIds) {
		return client.post("resourcesStatus", MonitorTargetStatus[].class, resourceIds);
	}
}
