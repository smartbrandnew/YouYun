package com.broada.carrier.monitor.server.impl.pmdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.common.entity.DefaultDynamicObject;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.common.restful.BaseClient;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.client.restful.RestfulServerServiceFactory;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTarget;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetGroup;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.carrier.monitor.server.api.service.ServerNodeService;
import com.broada.carrier.monitor.server.api.service.ServerResourceService;
import com.broada.carrier.monitor.server.api.service.ServerTargetTypeService;
import com.broada.carrier.monitor.server.impl.config.Config;
import com.broada.carrier.monitor.server.impl.pmdb.entity.PMDBPage;
import com.broada.utils.TextUtil;

public class RestfulPMDBClient implements PMDBClient {
	private BaseClient client;
	private ServerTargetTypeService targetTypeService;
	private ServerNodeService nodeService;
	private ServerResourceService resourceService;
	
	public RestfulPMDBClient(String baseServiceUrl, ServerTargetTypeService targetTypeService, 
			ServerNodeService nodeService, ServerResourceService resourceService) {
		client = new BaseClient(baseServiceUrl, "/ciGroups", "PMDBClient");
		this.targetTypeService = targetTypeService;
		this.nodeService = nodeService;
		this.resourceService = resourceService;
	}

	@Override
	public MonitorTargetGroup[] getGroups(String userId, String domainId, String parentId) {
		DefaultDynamicObject[] maps = client.get(DefaultDynamicObject[].class, "domainId", domainId, "userId", userId);
		if (maps == null || maps.length == 0)
			return new MonitorTargetGroup[0];
		
		List<MonitorTargetGroup> list = new ArrayList<MonitorTargetGroup>(); 
		for (DefaultDynamicObject obj : maps) {
			append(list, obj, null);
		}
		
		List<MonitorTargetGroup> result = new ArrayList<MonitorTargetGroup>();
		for (MonitorTargetGroup group : list) {
			MonitorTargetType type = targetTypeService.getTargetType(group.getTargetTypeId());
			if (type == null)
				continue;
			if (!(type.isNode() || type.isResource()))
					continue;
			if (parentId == null && group.getParentId() == null)
				result.add(group);
			else if (parentId != null && parentId.equals(group.getParentId()))
				result.add(group);
		}
		
		return result.toArray(new MonitorTargetGroup[result.size()]);
	}

	@SuppressWarnings("unchecked")
	private void append(List<MonitorTargetGroup> list, DefaultDynamicObject obj, MonitorTargetGroup parent) {
		MonitorTargetGroup self = toTargetGroup(obj, parent); 
		if (self == null)
			return;
		
		list.add(self);		
		List<Object> childs = (List<Object>) obj.get("children");
		if (childs != null) {
			for (Object child : childs) {
				DefaultDynamicObject cho = new DefaultDynamicObject((Map<String, Object>)child);
				append(list, cho, self);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private MonitorTargetGroup toTargetGroup(DefaultDynamicObject obj, MonitorTargetGroup parent) {
		List<Object> filters = (List<Object>) obj.get("filters");
		if (filters == null || filters.isEmpty()) 
			return null;
				
		DefaultDynamicObject fo = new DefaultDynamicObject((Map<String, Object>)filters.get(0));
		String code = fo.checkString("targetClzCode");	
		MonitorTargetType type = targetTypeService.getTargetType(code); 
		if (type == null)
			return null;
		
		String id = obj.checkString("id");
		String name = obj.checkString("name");
		String domainId = obj.checkString("owner");
		return new MonitorTargetGroup(id, name, parent != null ? parent.getId() : null, type.getId(), domainId);				
	}

	@Override
	public MonitorTarget[] getGroupInstances(PageNo pageNo, String groupId, String ipKey) {		
		List<Object> params = new ArrayList<Object>();
		params.add("page");
		params.add(pageNo.getIndex());
		params.add("size");
		params.add(pageNo.getSize());
		params.add("dataRegion");
		params.add("global");
		if (!TextUtil.isEmpty(ipKey)) {
			params.add("query");
			params.add("ipAddr:LIKE:" + ipKey);
		}
		PMDBPage page = client.get(groupId + "/cis", PMDBPage.class, params.toArray());
		if (page == null || page.getData() == null || page.getData().length == 0)
			return new MonitorTarget[0];
		
		List<MonitorTarget> list = toTarget(page.getData());
		return list.toArray(new MonitorTarget[list.size()]);
	}

	@SuppressWarnings("unchecked")
	private List<MonitorTarget> toTarget(DefaultDynamicObject[] objs) {
		List<MonitorTarget> list = new ArrayList<MonitorTarget>();
		
		Map<String, DefaultDynamicObject> map = new HashMap<String, DefaultDynamicObject>(objs.length);
		List<String> listTmpNodeIds = new ArrayList<String>();
		List<String> listTmpResourceIds = new ArrayList<String>();

		for (DefaultDynamicObject obj : objs) {
			MonitorTargetType type = checkTargetType(obj.checkString("templateCode"));
			if (type.isNode()) {
				listTmpNodeIds.add(obj.checkString("id"));
			} else {
				listTmpResourceIds.add(obj.checkString("id"));
			}
			map.put(obj.checkString("id"), obj);
		}

		Map<String, Integer> mapNodes = nodeService.getNodeProbeId(listTmpNodeIds.toArray(new String[0]));
		Map<String, String> mapResourceNodeId = resourceService.getResourceNodeId(listTmpResourceIds.toArray(new String[0]));

		for (String id : map.keySet()) {
			DefaultDynamicObject obj = map.get(id);
			DefaultDynamicObject values = new DefaultDynamicObject((Map<String, Object>) (obj.get("values")));
			MonitorTargetType type = checkTargetType(obj.checkString("templateCode"));
			MonitorTarget target;
			if (type.isNode()) {
				MonitorNode node = new MonitorNode();
				target = node;
				node.setIp(values.get("ipAddr", ""));
				node.setProbeId(mapNodes.get(id) == null ? 0 : mapNodes.get(id));
			} else {
				MonitorResource resource = new MonitorResource();
				target = resource;
				resource.setNodeId(mapResourceNodeId.get(id));
			}
			target.setId(obj.checkString("id"));
			target.setName(values.get("name", obj.checkString("displayName")));
			target.setTypeId(type.getId());
			target.setModified(obj.checkDate("updateTime").getTime());
			target.setAuditState(PMDBConverter.toTargetAuditState(obj.get("status", "apply")));
			list.add(target);
		}

		return list;
	}

	private MonitorTargetType checkTargetType(String targetTypeId) {
		return ServerUtil.checkTargetType(targetTypeService, targetTypeId);		
	}
}
