package com.broada.carrier.monitor.server.impl.logic.trans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.common.error.DataAccessException;
import com.broada.carrier.monitor.common.util.TextUtil;
import com.broada.carrier.monitor.server.api.client.EventListener;
import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.MonitorTarget;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetGroup;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetStatus;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.event.NodeChangedEvent;
import com.broada.carrier.monitor.server.api.event.ObjectChangedType;
import com.broada.carrier.monitor.server.api.event.RecordChangedEvent;
import com.broada.carrier.monitor.server.api.event.TargetStatusChangedEvent;
import com.broada.carrier.monitor.server.api.event.TaskChangedEvent;
import com.broada.carrier.monitor.server.api.service.ServerNodeService;
import com.broada.carrier.monitor.server.api.service.ServerTargetTypeService;
import com.broada.carrier.monitor.server.impl.dao.NodeDao;
import com.broada.carrier.monitor.server.impl.entity.ServerSideMonitorNode;
import com.broada.carrier.monitor.server.impl.logic.EventBus;
import com.broada.carrier.monitor.server.impl.logic.ServerLogicUtil;
import com.broada.carrier.monitor.server.impl.logic.SessionManager;
import com.broada.carrier.monitor.server.impl.pmdb.PMDBConverter;
import com.broada.carrier.monitor.server.impl.pmdb.PMDBFacade;
import com.broada.cmdb.api.data.Instance;
import com.broada.cmdb.api.model.QueryOperator;
import com.broada.cmdb.api.model.QueryRequest;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.lang.SimpleProperties;
import com.broada.utils.StringUtil;

import edu.emory.mathcs.backport.java.util.Arrays;

public class ServerNodeServiceTrans implements ServerNodeService {
	@Autowired
	private PMDBFacade pmdbFacade;
	@Autowired
	private NodeDao dao;
	@Autowired
	private ServerTargetTypeService targetTypeService;
	@Autowired
	private ServerServiceFactory serverFactory;
	private Map<String, MonitorTargetStatus> nodeStatuses = new ConcurrentHashMap<String, MonitorTargetStatus>();
	private static Boolean nodeShowSign;

	public ServerNodeServiceTrans() {
		EventBus.getDefault().registerObjectChangedListener(new MyEventListener());
	}

	private class MyEventListener implements EventListener {
		@Override
		public void receive(Object event) {
			if (event instanceof TaskChangedEvent)
				processEvent((TaskChangedEvent) event);
			else if (event instanceof RecordChangedEvent)
				processEvent(((RecordChangedEvent) event).getObject());
		}

		private void processEvent(MonitorRecord record) {
			if (!record.isStateChanged())
				return;

			MonitorTask task = serverFactory.getTaskService().getTask(record.getTaskId());
			if (task == null)
				return;

			MonitorTargetStatus status = nodeStatuses.get(task.getNodeId());
			if (status == null)
				return;

			if (status.getMonitorState() == record.getState())
				return;

			if (record.getState() == MonitorState.FAILED)
				status.setMonitorState(record.getState());
			
			else {
				MonitorTask[] tasks = serverFactory.getTaskService().getTasksByNodeId(task.getNodeId());
				status.set(ServerLogicUtil.getMonitorTargetStatus(serverFactory, task.getNodeId(), tasks));
			}
			EventBus.getDefault().publishObjectChanged(new TargetStatusChangedEvent(ObjectChangedType.CREATED, null, status));
		}

		private void processEvent(TaskChangedEvent event) {
			if (event.getType() == ObjectChangedType.UPDATED)
				return;

			MonitorTargetStatus status = nodeStatuses.get(event.getObject().getNodeId());
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
		String showAuditNodesFordel = props.get("client.refresh.contains.object.deleted.auditing");
		return Boolean.parseBoolean(showAuditNodesFordel);
	}
	
	@Override
	public Page<MonitorNode> getNodesByGroupId(PageNo pageNo, String groupId) {
		MonitorTarget[] targets = pmdbFacade.getGroupInstances(pageNo, groupId, null);
		ArrayList<MonitorNode> rows = new ArrayList<MonitorNode>(targets.length);
		for (int i = 0; i < targets.length; i++) {
			if (targets[i] instanceof MonitorNode){
				MonitorNode node = (MonitorNode) targets[i];
				node.setDomainId(SessionManager.checkSessionDomainId());
				rows.add(node);
			}
		}
		return new Page<MonitorNode>(rows, pageNo, new MonitorNode[0]);
	}

	private void fill(MonitorNode node) {
		node.setProbeId(getNodeProbeId(node.getId()));
	}

	@Override
	public Page<MonitorNode> getNodesByProbeId(PageNo pageNo, int probeId, boolean currentDomain) {
		if(nodeShowSign == null)
			nodeShowSign = getProperty();
		String domainId = currentDomain ? SessionManager.checkSessionDomainId() : null;
		ServerSideMonitorNode[] locals = dao.getByProbeId(domainId, probeId);
		ArrayList<MonitorNode> nodes = new ArrayList<MonitorNode>(locals.length);
		MonitorNode node;
		for (ServerSideMonitorNode local : locals) {
			Instance inst = pmdbFacade.getInstanceById(local.getId());
			if (nodeShowSign) {
				String status = inst == null ? null : inst.getStatus().trim();
				if (inst == null || (status != null && "delete".equals(status)))
					continue;
				node = PMDBConverter.toNode(inst, domainId);
				local.get(node);
				nodes.add(node);
			} else {
				if (inst == null)
					continue;
				node = PMDBConverter.toNode(inst, domainId);
				local.get(node);
				nodes.add(node);
			}
		}
		return new Page<MonitorNode>(nodes, pageNo, new MonitorNode[0]);
	}
	
	@Override
	public Page<MonitorNode> getNodes(boolean currentDomain) {
		return new Page<MonitorNode>(getNodes(null ,currentDomain), PageNo.ALL);
	}
	
	/**
	 * 根据id获取节点
	 * @param ids 如果传入ids为null或空，则返回所有节点
	 * @param currentDomain
	 */
	public MonitorNode[] getNodes(List<String> ids, boolean currentDomain) {
		if(nodeShowSign == null)
			nodeShowSign = getProperty();
		String domainId = currentDomain ? SessionManager.checkSessionDomainId() : null;
		
		ServerSideMonitorNode[] locals = null;
		if(ids == null || ids.size() == 0)
			locals = dao.getNodes(domainId);
		else
			locals = dao.getNodes(ids, domainId);
		
		List<String> list = new ArrayList<String>();
		for(ServerSideMonitorNode local : locals){
			list.add(local.getId());
		}
		
		List<Instance> insts = pmdbFacade.getInstanceByIds(list);
		
		Map<String, Instance> map = new HashMap<String, Instance>();
		if(insts != null)
			for(Instance inst : insts){
				map.put(inst.getId(), inst);
			}
		
		List<MonitorNode> nodes = new ArrayList<MonitorNode>(locals.length);
		MonitorNode node;
		for (int i = 0; i < locals.length; i++) {
			ServerSideMonitorNode local = locals[i];
			Instance inst = map.get(local.getId());
			if (nodeShowSign) {
				String status = inst == null ? null : inst.getStatus().trim();
				if (inst == null || (status != null && "delete".equals(status)))
					continue;
				node = PMDBConverter.toNode(inst, domainId);
				local.get(node);
				nodes.add(node);
			} else {
				if (inst == null)
					continue;
				node = PMDBConverter.toNode(inst, domainId);
				local.get(node);
				nodes.add(node);
			}
		}
		return nodes.toArray(new MonitorNode[0]);
	}
	
	@Override
	public String saveNode(MonitorNode node) {
		ObjectChangedType changedType = ObjectChangedType.UPDATED;
		if (TextUtil.isEmpty(node.getId())) {
			changedType = ObjectChangedType.CREATED;
			MonitorNode exists = getNode(node.getTypeId(), node.getIp());
			if (exists != null)
				throw new IllegalArgumentException(String.format("节点IP[%s]已经存在，无法重复添加", node.getIp()));
		}

		Instance inst = PMDBConverter.toInstance(node);
		inst.setResourceChangePersonId(SessionManager.checkSessionUserId());
		node.setId(pmdbFacade.saveInstance(inst, null, StringUtil.isNullOrBlank(node.getDomainId()) ? SessionManager.checkSessionDomainId() : node.getDomainId()));
		try {
			if (node.getId() != null)
				saveNodeLocal(node);
			EventBus.getDefault().publishObjectChanged(new NodeChangedEvent(changedType, null, node));
			return node.getId();
		} catch (Throwable e) {
			deleteNode(node.getId());
			throw new DataAccessException(ErrorUtil.createMessage("保存节点探针信息失败，放弃配置项保存", e), e);
		}
	}

	private MonitorNode getNode(String typeId, String ip) {
		QueryRequest request = new QueryRequest();
		request.addCondition(PMDBConverter.ATTR_IP, QueryOperator.EQUALS, ip);
		Instance[] insts = pmdbFacade.getInstanceByQuery(typeId, request);
		if (insts == null || insts.length <= 0)
			return null;
		else
			return PMDBConverter.toNode(insts[0], pmdbFacade.checkInstanceDomainIdById(insts[0].getId()));
	}

	private void saveNodeLocal(MonitorNode node) {
		ServerSideMonitorNode local = dao.get(node.getId());
		if (local == null)
			local = new ServerSideMonitorNode(node);
		else if (local.getProbeId() == node.getProbeId())
			return;
		else
			local.set(node);
		dao.save(local);
	}

	@Override
	public OperatorResult deleteNode(String id) {
		MonitorResource[] resources = serverFactory.getResourceService().getResourcesByNodeId(id);
		for (MonitorResource resource : resources)
			serverFactory.getResourceService().deleteResource(resource.getId());

		pmdbFacade.deleteInstanceById(id);

		Instance instance = pmdbFacade.getInstanceById(id);
		MonitorNode node = new MonitorNode();
		node.setId(id);
		if (instance == null) {
			EventBus.getDefault().publishObjectChanged(new NodeChangedEvent(ObjectChangedType.DELETED, node, null));
			return OperatorResult.DELETED;
		} else {
			instance.setResourceChangePersonId(SessionManager.checkSessionUserId());
			EventBus.getDefault().publishObjectChanged(new NodeChangedEvent(ObjectChangedType.UPDATED, null, node));
			return OperatorResult.MODIFIED;
		}
	}

	@Override
	public MonitorNode getNode(String nodeId) {
		Instance inst = pmdbFacade.getInstanceById(nodeId);
		if (inst == null)
			return null;

		String domainId = pmdbFacade.checkInstanceDomainIdById(nodeId);
		MonitorNode node = PMDBConverter.toNode(inst, domainId);
		if (node != null)
			fill(node);
		return node;
	}

	@Override
	public int getNodeProbeId(String nodeId) {
		ServerSideMonitorNode local = dao.get(nodeId);
		if (local == null)
			return 0;
		return local.getProbeId();
	}
	
	@Override
	public Map<String, Integer> getNodeProbeId(String[] nodeIds) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		if(nodeIds == null || nodeIds.length == 0)
			return map;
		
		@SuppressWarnings("unchecked")
		ServerSideMonitorNode[] locals = dao.getNodes(Arrays.asList(nodeIds), SessionManager.checkSessionDomainId());
		if (locals == null || locals.length == 0)
			return map;
		
		for(ServerSideMonitorNode local : locals)
			map.put(local.getId(), local.getProbeId());
		return map;
	}

	@Override
	public Page<MonitorNode> getNodesByIp(PageNo pageNo, String ip) {
		String userId = SessionManager.checkSessionUserId();
		String domainId = SessionManager.checkSessionDomainId();
		MonitorTargetGroup[] groups = pmdbFacade.getGroups(userId, domainId, null);
		if (groups == null || groups.length == 0)
			return new Page<MonitorNode>();

		LinkedList<MonitorTargetGroup> queue = new LinkedList<MonitorTargetGroup>();
		for (MonitorTargetGroup group : groups) {
			queue.push(group);
		}

		PageNo getPage;
		if (pageNo.getIndex() == 0)
			getPage = PageNo.createByIndex(0, pageNo.getSize() + 1);
		else
			getPage = PageNo.createByIndex(0, pageNo.getLast() + 1);

		List<MonitorNode> nodes = new ArrayList<MonitorNode>();
		while (queue.size() > 0) {
			MonitorTargetGroup group = queue.pop();
			MonitorTargetType type = targetTypeService.getTargetType(group.getTargetTypeId());
			if (!type.isNode())
				continue;

			MonitorTarget[] targets = pmdbFacade.getGroupInstances(getPage, group.getId(), ip);
			if (targets == null)
				continue;

			for (MonitorTarget target : targets)
				nodes.add((MonitorNode) target);

			if (nodes.size() >= getPage.getLast())
				break;
		}
		return new Page<MonitorNode>(nodes, pageNo, new MonitorNode[0]);
	}

	@Override
	public MonitorTargetStatus getNodeStatus(String nodeId) {
		MonitorTargetStatus nodeStatus = nodeStatuses.get(nodeId);
		if (nodeStatus == null) {
			MonitorTask[] tasks = serverFactory.getTaskService().getTasksByNodeId(nodeId);
			nodeStatus = ServerLogicUtil.getMonitorTargetStatus(serverFactory, nodeId, tasks);
			nodeStatuses.put(nodeId, nodeStatus);
		}
		return nodeStatus;
	}
	
	@Override
	public MonitorTargetStatus[] getNodesStatus(String[] nodeIds) {
		List<MonitorTargetStatus> listExist = new ArrayList<MonitorTargetStatus>();
		List<String> listNotExist = new ArrayList<String>();
		for(String nodeId : nodeIds){
			if(nodeStatuses.get(nodeId) == null)
				listNotExist.add(nodeId);
			else
				listExist.add(nodeStatuses.get(nodeId));
		}
		MonitorTask[] tasks = serverFactory.getTaskService().getTasksByNodeIds(listNotExist.toArray(new String[0]));
		MonitorTargetStatus[] nodesStatus = ServerLogicUtil.getMonitorTargetsStatus(serverFactory, listNotExist.toArray(new String[0]), tasks, MonitorNode.class);
		for(MonitorTargetStatus nodeStatus : nodesStatus){
			nodeStatuses.put(nodeStatus.getTargetId(), nodeStatus);
			listExist.add(nodeStatus);
		}
		
		return listExist.toArray(new MonitorTargetStatus[0]);
	}

}
