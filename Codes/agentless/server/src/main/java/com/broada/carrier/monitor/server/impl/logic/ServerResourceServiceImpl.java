package com.broada.carrier.monitor.server.impl.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.common.util.TextUtil;
import com.broada.carrier.monitor.server.api.client.EventListener;
import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.MonitorTarget;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetStatus;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.event.NodeChangedEvent;
import com.broada.carrier.monitor.server.api.event.ObjectChangedType;
import com.broada.carrier.monitor.server.api.event.RecordChangedEvent;
import com.broada.carrier.monitor.server.api.event.ResourceChangedEvent;
import com.broada.carrier.monitor.server.api.event.TargetStatusChangedEvent;
import com.broada.carrier.monitor.server.api.event.TaskChangedEvent;
import com.broada.carrier.monitor.server.api.service.ServerResourceService;
import com.broada.carrier.monitor.server.impl.pmdb.PMDBConverter;
import com.broada.carrier.monitor.server.impl.pmdb.PMDBFacade;
import com.broada.cmdb.api.data.Instance;
import com.broada.cmdb.api.data.Relationship;
import com.broada.cmdb.api.model.QueryOperator;
import com.broada.cmdb.api.model.QueryRequest;
import com.broada.component.utils.lang.SimpleProperties;

public class ServerResourceServiceImpl implements ServerResourceService {
	private static final Logger logger = LoggerFactory.getLogger(ServerResourceServiceImpl.class);
	@Autowired
	private PMDBFacade pmdbFacade;
	@Autowired
	private ServerServiceFactory serverFactory;
	private Map<String, MonitorTargetStatus> resourceStatuses = new ConcurrentHashMap<String, MonitorTargetStatus>();
	private static Boolean resShowSign;

	public ServerResourceServiceImpl() {
		EventBus.getDefault().registerObjectChangedListener(new MyEventListener());
	}

	private class MyEventListener implements EventListener {
		@Override
		public void receive(Object event) {
			logger.debug("收到事件：{}", event);
			if (event instanceof TaskChangedEvent)
				processEvent((TaskChangedEvent) event);
			else if (event instanceof RecordChangedEvent)
				processEvent(((RecordChangedEvent) event).getObject());
			else if (event instanceof NodeChangedEvent)
				processEvent((NodeChangedEvent) event);
		}

		private void processEvent(NodeChangedEvent event) {
		}

		private void processEvent(MonitorRecord record) {
			if (!record.isStateChanged())
				return;

			MonitorTask task = serverFactory.getTaskService().getTask(record.getTaskId());
			if (task == null)
				return;

			if (task.getResourceId() == null)
				return;

			MonitorTargetStatus status = resourceStatuses.get(task.getResourceId());
			if (status == null)
				return;

			if (status.getMonitorState() == record.getState())
				return;

			if (record.getState() == MonitorState.FAILED)
				status.setMonitorState(record.getState());
			
			else {
				MonitorTask[] tasks = serverFactory.getTaskService().getTasksByResourceId(task.getResourceId());
				status.set(ServerLogicUtil.getMonitorTargetStatus(serverFactory, task.getResourceId(), tasks));
			}
			EventBus.getDefault().publishObjectChanged(new TargetStatusChangedEvent(ObjectChangedType.CREATED, null, status));
		}

		private void processEvent(TaskChangedEvent event) {
			if (event.getType() == ObjectChangedType.UPDATED)
				return;

			if (event.getObject().getResourceId() == null)
				return;

			MonitorTargetStatus status = resourceStatuses.get(event.getObject().getResourceId());
			if (status == null)
				return;

			if (event.getType() == ObjectChangedType.CREATED)
				status.setTaskCount(status.getTaskCount() + 1);
			else {
				status.setTaskCount(status.getTaskCount() - 1);
				if (status.getTaskCount() == 0)
					status.setMonitorState(MonitorState.UNMONITOR);
			}
			EventBus.getDefault().publishObjectChanged(new TargetStatusChangedEvent(ObjectChangedType.CREATED, null, status));
		}
	}
	
	private Boolean getProperty(){
		SimpleProperties props = new SimpleProperties(System.getProperty("monitor.config"));
		String showAuditResFordel = props.get("client.refresh.contains.object.deleted.auditing");
		return Boolean.parseBoolean(showAuditResFordel);
	}

	@Override
	public Page<MonitorResource> getResourcesByGroupId(PageNo pageNo, String groupId) {
		MonitorTarget[] targets = pmdbFacade.getGroupInstances(pageNo, groupId, null);
		ArrayList<MonitorResource> rows = new ArrayList<MonitorResource>(targets.length);
		for (int i = 0; i < targets.length; i++) {
			if (targets[i] instanceof MonitorResource)
				rows.add((MonitorResource) targets[i]);
		}
		return new Page<MonitorResource>(rows, pageNo, new MonitorResource[0]);
	}

	@Override
	public String getResourceNodeId(String resourceId) {
		String nodeId = pmdbFacade.getNodeIdByResourceId(resourceId);
		MonitorNode node = serverFactory.getNodeService().getNode(nodeId);
		if (node == null)
			return null;
		return nodeId;
	}
	
	@Override
	public Map<String, String> getResourceNodeId(String[] resourceIds) {
		Map<String, String> map = new HashMap<String, String>();
		if(resourceIds == null || resourceIds.length == 0)
			return map;
		
		// oracle数据库对in查询做了限制，最多1000个，mysql无限制
		int maxQuery = 1000;
		
		// 从RunningOn中找到resourcesIds
		QueryRequest request = new QueryRequest();
		StringBuffer sb = new StringBuffer();
		List<String> list = Arrays.asList(resourceIds);
		int size = list.size() % maxQuery;
		if(size == 0)
			size = list.size() / maxQuery - 1;
		else
			size = list.size() / maxQuery;
		
		for(int i = 0; i <= size; i++){
			if(i == 0)
				sb.append(" src in ('");
			else
				sb.append(" or src in ('");
			if(i != size)
				sb.append(StringUtils.join(list.subList(i * maxQuery, i * maxQuery + maxQuery), "','"));
			else
				sb.append(StringUtils.join(list.subList(i * maxQuery, list.size()), "','"));
			sb.append("')");
		}
		request.addCondition("extQuerySql", QueryOperator.EQUALS, sb.toString());
		Relationship[] rss = pmdbFacade.getRelationshipByQuery(PMDBConverter.NODE_RESOURCE_RT, request);
		if(rss.length == 0)
			return map;
		
		for(Relationship rs : rss){
			map.put(rs.getSrc(), rs.getDest());
		}
		return map;
	}

	@Override
	public MonitorResource[] getResourcesByNodeId(String nodeId) {
		String[] ids = pmdbFacade.getResourceIdsByNodeId(nodeId);
		ArrayList<MonitorResource> list = new ArrayList<MonitorResource>();
		for (String id : ids) {
			MonitorResource res = getResource(id);
			if (res != null)
				list.add(res);
		}
		return list.toArray(new MonitorResource[list.size()]);
	}

	@Override
	public MonitorResource getResource(String resourceId) {
		if(resShowSign == null)
			resShowSign = getProperty();
		
		Instance inst = pmdbFacade.getInstanceById(resourceId);
		String domainId;
		MonitorResource res;
		if (resShowSign) {
			String status = inst == null ? null : inst.getStatus();
			if (inst == null || status != null && "delete".equals(status))
				return null;
			domainId = pmdbFacade.checkInstanceDomainIdById(resourceId);
			res = PMDBConverter.toResource(inst, domainId);

		} else {
			if (inst == null)
				return null;
			domainId = pmdbFacade.checkInstanceDomainIdById(resourceId);
			res = PMDBConverter.toResource(inst, domainId);
		}
		String nodeId = getResourceNodeId(res.getId());
		if (nodeId != null){
			res.setNodeId(nodeId);
			return res;
		}
		return null;
	}
	
	@Override
	public Page<MonitorResource> getResourcesByNodeIds(String nodeIds) {
		if(resShowSign == null)
			resShowSign = getProperty();
		
		int maxQuery = 1000;//oracle数据库对in查询做了限制，最多1000个，mysql无限制
		
		// 从RunningOn中找到resourcesIds
		QueryRequest request = new QueryRequest();
		StringBuffer sb = new StringBuffer();
		List<String> list = Arrays.asList(nodeIds.split(","));
		int size = list.size() % maxQuery;
		if(size == 0)
			size = list.size() / maxQuery - 1;
		else
			size = list.size() / maxQuery;
		
		for(int i = 0; i <= size; i++){
			if(i == 0)
				sb.append(" dest in ('");
			else
				sb.append(" or dest in ('");
			if(i != size)
				sb.append(StringUtils.join(list.subList(i * maxQuery, i * maxQuery + maxQuery), "','"));
			else
				sb.append(StringUtils.join(list.subList(i * maxQuery, list.size()), "','"));
			sb.append("')");
		}
		request.addCondition("extQuerySql", QueryOperator.EQUALS, sb.toString());
		Relationship[] rss = pmdbFacade.getRelationshipByQuery(PMDBConverter.NODE_RESOURCE_RT, request);
		if(rss.length == 0)
			return new Page<MonitorResource>(new ArrayList<MonitorResource>(), PageNo.ALL, new MonitorResource[0]);
		
		List<String> resourceIds = new ArrayList<String>();
		Map<String, String> mapRss = new HashMap<String, String>();
		for (int i = 0; i < rss.length; i++){
			resourceIds.add(rss[i].getSrc());
			mapRss.put(rss[i].getSrc(), rss[i].getDest());
		}
		// 根据ids找到Instance
		ArrayList<MonitorResource> resources = new ArrayList<MonitorResource>();
		request.addCondition("extQuerySql", QueryOperator.EQUALS, sb.toString());
		List<Instance> insts = pmdbFacade.getInstanceByIds(resourceIds);
		if(insts == null || insts.size() == 0)
			return new Page<MonitorResource>(resources, PageNo.ALL, new MonitorResource[0]);
		
		MonitorResource res;
		for(Instance inst : insts){
			if (resShowSign) {
				String status = inst.getStatus();
				if (status != null && "delete".equals(status))
					continue;
				res = PMDBConverter.toResource(inst, null);
				res.setNodeId(mapRss.get(res.getId()));
			} else {
				res = PMDBConverter.toResource(inst, null);
				res.setNodeId(mapRss.get(res.getId()));
			}
			resources.add(res);
		}
		return new Page<MonitorResource>(resources, PageNo.ALL, new MonitorResource[0]);
	}

	@Override
	public String saveResource(MonitorResource resource) {
		logger.debug("保存资源：{}", resource);
		ObjectChangedType changedType = ObjectChangedType.UPDATED;
		if (TextUtil.isEmpty(resource.getId()))
			changedType = ObjectChangedType.CREATED;

		if (changedType == ObjectChangedType.CREATED) {
			MonitorNode node = ServerUtil.checkNode(serverFactory.getNodeService(), resource.getNodeId());
			if (!pmdbFacade.isResourceRunningOnNode(resource.getTypeId(), node.getTypeId()))
				throw new IllegalArgumentException("资源[" + resource.getTypeId() + "]不允许添加到节点[" + node.getTypeId()
						+ "]类型，不存在此关系模式");
		}

		Instance inst = PMDBConverter.toInstance(resource);
		if (inst != null)
			inst.setResourceChangePersonId(SessionManager.checkSessionUserId());
		Relationship[] rss = new Relationship[] {
				new Relationship(PMDBConverter.NODE_RESOURCE_RT, resource.getId(), resource.getNodeId())
		};
		resource.setId(pmdbFacade.saveInstance(inst, rss, SessionManager.checkSessionDomainId()));
		EventBus.getDefault().publishObjectChanged(new ResourceChangedEvent(changedType, null, resource));
		return resource.getId();
	}

	@Override
	public OperatorResult deleteResource(String id) {
		logger.debug("删除资源，资源ID：{}", id);
		pmdbFacade.deleteInstanceById(id);
		Instance instance = pmdbFacade.getInstanceById(id);
		MonitorResource resource = new MonitorResource();
		resource.setId(id);
		if (instance == null) {
			EventBus.getDefault().publishObjectChanged(new ResourceChangedEvent(ObjectChangedType.DELETED, resource, null));
			return OperatorResult.DELETED;
		} else {
			instance.setResourceChangePersonId(SessionManager.checkSessionUserId());
			EventBus.getDefault().publishObjectChanged(new ResourceChangedEvent(ObjectChangedType.UPDATED, null, resource));
			return OperatorResult.MODIFIED;
		}
	}

	public void deleteResourceByNodeId(String nodeId) {
		logger.debug("删除节点资源，节点ID：{}", nodeId);
		MonitorResource[] resources = getResourcesByNodeId(nodeId);
		for (MonitorResource resource : resources)
			deleteResource(resource.getId());
	}

	@Override
	public MonitorTargetStatus getResourceStatus(String resourceId) {
		MonitorTargetStatus resourceStatus = resourceStatuses.get(resourceId);
		if (resourceStatus == null) {
			MonitorTask[] tasks = serverFactory.getTaskService().getTasksByResourceId(resourceId);
			resourceStatus = ServerLogicUtil.getMonitorTargetStatus(serverFactory, resourceId, tasks);
			resourceStatuses.put(resourceId, resourceStatus);
		}
		return resourceStatus;
	}
	
	@Override
	public MonitorTargetStatus[] getResourcesStatus(String[] resourceIds) {
		List<MonitorTargetStatus> listExist = new ArrayList<MonitorTargetStatus>();
		List<String> listNotExist = new ArrayList<String>();
		for(String resourceId : resourceIds){
			if(resourceStatuses.get(resourceId) == null)
				listNotExist.add(resourceId);
			else
				listExist.add(resourceStatuses.get(resourceId));
		}
		MonitorTask[] tasks = serverFactory.getTaskService().getTasksByResourceIds(listNotExist.toArray(new String[0]));
		MonitorTargetStatus[] resourcesStatus = ServerLogicUtil.getMonitorTargetsStatus(serverFactory, listNotExist.toArray(new String[0]), tasks, MonitorResource.class);
		for(MonitorTargetStatus resourceStatus : resourcesStatus){
			resourceStatuses.put(resourceStatus.getTargetId(), resourceStatus);
			listExist.add(resourceStatus);
		}
		
		return listExist.toArray(new MonitorTargetStatus[0]);
	}

}
