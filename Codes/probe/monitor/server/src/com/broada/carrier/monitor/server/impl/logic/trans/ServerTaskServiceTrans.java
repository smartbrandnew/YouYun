package com.broada.carrier.monitor.server.impl.logic.trans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.acm.IllegalArgumentException;
import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.CollectParams;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.CollectTaskSign;
import com.broada.carrier.monitor.server.api.entity.ExecuteParams;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.TestParams;
import com.broada.carrier.monitor.server.api.service.ServerMethodService;
import com.broada.carrier.monitor.server.api.service.ServerNodeService;
import com.broada.carrier.monitor.server.api.service.ServerResourceService;
import com.broada.carrier.monitor.server.impl.dao.InstanceDao;
import com.broada.carrier.monitor.server.impl.dao.TaskDao;
import com.broada.carrier.monitor.server.impl.entity.LicenseInfo;
import com.broada.carrier.monitor.server.impl.entity.ServerSideMonitorTask;
import com.broada.carrier.monitor.server.impl.entity.StateLast;
import com.broada.carrier.monitor.server.impl.entity.StateType;
import com.broada.carrier.monitor.server.impl.logic.AlertSender;
import com.broada.carrier.monitor.server.impl.logic.ServerSystemServiceImpl;
import com.broada.carrier.monitor.server.impl.logic.ServerTaskServiceEx;
import com.broada.carrier.monitor.server.impl.logic.SessionManager;
import com.broada.carrier.monitor.server.impl.pmdb.map.PMDBMapper;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.lang.SimpleProperties;

public class ServerTaskServiceTrans implements ServerTaskServiceEx {
	private static final Logger logger = LoggerFactory.getLogger(ServerTaskServiceTrans.class);
	@Autowired
	private PMDBMapper pmdbMapper;
	@Autowired
	private TaskDao dao;
	@Autowired
	private InstanceDao instanceDao;
	@Autowired
	private ServerNodeService nodeService;
	@Autowired
	private ServerResourceService resourceService;
	@Autowired
	private ServerMethodService methodService;
	@Autowired
	private ServerStateServiceTrans stateService;
	@Autowired
	private ServerServiceFactory serverFactory;
	
	private Boolean nodeShowSign = null;
	
	/**
	 * 对监测任务进行校验，是否有效
	 * @param taskArr 需要校验的监测任务
	 * @param currentDomain 是否使用域
	 * @param ifQueryAll 是否需要查询所有节点信息（用于提高查询效率，当查询所有任务时传true，否则传false）
	 * @return
	 */
	private MonitorTask[] taskIntercept(ServerSideMonitorTask[] taskArr, boolean currentDomain, boolean ifQueryAll){
		if(nodeShowSign == null){
			SimpleProperties props = new SimpleProperties(System.getProperty("monitor.config"));
			String showAuditNodesFordel = props.get("client.refresh.contains.object.deleted.auditing");
			nodeShowSign = Boolean.parseBoolean(showAuditNodesFordel);
		}
		List<MonitorTask> tasks = new ArrayList<MonitorTask>();
		if(nodeShowSign){
			MonitorNode[] nodes = null;
			if(ifQueryAll)
				nodes = serverFactory.getNodeService().getNodes(currentDomain).getRows();
			else{
				Set<String> idSet = new HashSet<String>();
				for(ServerSideMonitorTask sst : taskArr)
					idSet.add(sst.getNodeId());
				if(idSet == null || idSet.size() == 0)
					return new MonitorTask[0];
				nodes = serverFactory.getNodeService().getNodes(new ArrayList<String>(idSet),currentDomain);
			}
			if(nodes == null)
				return new MonitorTask[0];
			Map<String, MonitorNode> mapNodes = new HashMap<String, MonitorNode>();
			for(MonitorNode node : nodes){
				mapNodes.put(node.getId(), node);
			}
			
			StringBuffer ids = new StringBuffer();
			for(MonitorNode node : nodes){
				ids.append(node.getId()).append(",");
			}
			MonitorResource[] resources = serverFactory.getResourceService().getResourcesByNodeIds(ids.toString()).getRows();
			Map<String, MonitorResource> mapResources = new HashMap<String, MonitorResource>();
			if(resources != null)
				for(MonitorResource resource : resources){
					mapResources.put(resource.getId(), resource);
				}
			
			for(ServerSideMonitorTask task : taskArr){
				// 检查node有效性
				if(mapNodes.get(task.getNodeId()) == null)
					continue;
				// 检查resource有效性
				if(task.getResourceId() != null && mapResources.get(task.getResourceId()) == null)
					continue;
				tasks.add(task);
			}
		}
		return tasks.toArray(new MonitorTask[0]);
	}

	@Override
	public Page<MonitorTask> getTasksByProbeId(PageNo pageNo, int probeId, boolean currentDomain) {
		String domainId = currentDomain ? SessionManager.checkSessionDomainId() : null;
		Page<ServerSideMonitorTask> page = dao.getByProbeId(domainId, pageNo, probeId);
		return new Page<MonitorTask>(taskIntercept(page.getRows(), currentDomain, false), page.isMore());
	}

	@Override
	public Page<MonitorTask> getTasksByPolicyCode(PageNo pageNo, String policyCode) {
		Page<ServerSideMonitorTask> page = dao.getByPolicyCode(pageNo, policyCode);
		return new Page<MonitorTask>(taskIntercept(page.getRows(), false, false), page.isMore());
	}

	@Override
	public MonitorTask[] getTasksByNodeId(String nodeId) {
		return taskIntercept(dao.getByNodeId(nodeId), false, false);
	}
	
	@Override
	public MonitorTask[] getTasksByNodeIds(String[] nodeIds) {
		return taskIntercept(dao.getByNodeIds(nodeIds), false, false);
	}

	@Override
	public MonitorTask[] getTasksByResourceId(String resourceId) {
		return taskIntercept(dao.getByResourceId(resourceId), false, false);
	}
	
	@Override
	public MonitorTask[] getTasksByResourceIds(String[] resourceIds) {
		return taskIntercept(dao.getByResourceIds(resourceIds), false, false);
	}

	@Override
	public String saveTask(MonitorTask task) {
		MonitorNode node = ServerUtil.checkNode(nodeService, task.getNodeId());
		if (task.getResourceId() != null)
			ServerUtil.checkResource(resourceService, task.getResourceId());
		boolean checkResourceNum = task.getId() == null||task.getId().trim().length()<=0;
		ServerSideMonitorTask st;
		if (task instanceof ServerSideMonitorTask)
			st = (ServerSideMonitorTask) task;
		else
			st = new ServerSideMonitorTask(task);
		String taskId = dao.save(st);
		if (checkResourceNum)
			checkResourceNum(st, node);
		return taskId;
	}

	@Override
	public String saveTask(MonitorTask task, MonitorInstance[] instances) {
		String taskId = saveTask(task);
		instanceDao.deleteByTaskId(task.getId());
		if (instances != null) {
			for (MonitorInstance instance : instances) {
				instance.setTaskId(taskId);
				instanceDao.save(instance);
			}
		}
		return taskId;
	}

	@Override
	public int getLicenseUsedQuotaPCServer() {
		return dao.getTaskPCServerCount();
	}

	@Override
	public int getLicenseUsedQuotaMiniServer() {
		return dao.getTaskMiniServerCount();
	}

	@Override
	public int getLicenseUsedQuotaStorageDev() {
		return dao.getTaskStorageDevCount();
	}

	@Override
	public int getLicenseUsedQuotaAppPlatform() {
		return dao.getTaskDeviceCountByHaveRes();
	}

	private void checkResourceNum(MonitorTask task, MonitorNode node) {
		LicenseInfo licenseInfo = ServerSystemServiceImpl.getLicenseInfo();

		if (task.getResourceId() != null) {
			int count = getLicenseUsedQuotaAppPlatform();
			if (count > licenseInfo.getMonitorAppPlatform())
				throw new IllegalArgumentException("当前授权可以监控资源的设备数上限[" + licenseInfo.getMonitorAppPlatform() + "]已到，无法再添加监测任务");
		}

		MonitorTargetType type = ServerUtil.checkTargetType(serverFactory.getTargetTypeService(), node.getTypeId());
		boolean isMiniServer = type.getPath().startsWith("BaseDevice/Computer/Server")
				&& !type.getPath().equalsIgnoreCase("BaseDevice/Computer/Server/PCServer");
		if (isMiniServer) {
			int count = getLicenseUsedQuotaMiniServer();
			if (count > licenseInfo.getMonitorMiniServer())
				throw new IllegalArgumentException("当前授权可以监控的MiniServer上限[" + licenseInfo.getMonitorMiniServer()
						+ "]已到，无法再添加监测任务");
		} else {
			boolean isStorageDev = type.getPath().startsWith("BaseDevice/StorageDev");
			if (isStorageDev) {
				int count = getLicenseUsedQuotaStorageDev();
				if (count > licenseInfo.getMonitorStorageDev())
					throw new IllegalArgumentException("当前授权可以监控的存储设备上限[" + licenseInfo.getMonitorStorageDev() + "]已到，无法再添加监测任务");
			} else {
				int count = getLicenseUsedQuotaPCServer();
				if (count > licenseInfo.getMonitorPCServer())
					throw new IllegalArgumentException("当前授权可以监控的PCServer、VM、Computer上限[" + licenseInfo.getMonitorPCServer()
							+ "]已到，无法再添加监测任务");
			}
		}
	}

	@Override
	public void deleteTask(String taskId) {
		// 从mon_map中找到所有监测任务相关的cmdb实例，将它们删除并删除mon_map中对应的记录
		pmdbMapper.deleteInstance(taskId);
		// 删除mon_task中记录
		dao.delete(taskId);
		// 删除mon_instance中记录
		instanceDao.deleteByTaskId(taskId);
	}

	@Override
	public MonitorResult executeTask(String taskId, ExecuteParams params) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MonitorTask getTask(String taskId) {
		return dao.get(taskId);
	}

	@Override
	public Serializable collectTask(CollectParams params) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MonitorRecord getRecord(String taskId) {
		StateLast state = stateService.getStateLast(StateType.MONITOR_STATE, taskId);
		if (state != null)
			return new MonitorRecord(taskId,
					MonitorState.checkById(Integer.parseInt(state.getValue())),
					state.getLastTime(), state.getMessage(),
					state.getLastValue() == null ? MonitorState.UNMONITOR : MonitorState.checkById(Integer.parseInt(state
							.getLastValue())));

		synchronized (this) {
			state = stateService.getStateLast(StateType.MONITOR_STATE, taskId);
			if (state != null)
				return new MonitorRecord(taskId,
						MonitorState.checkById(Integer.parseInt(state.getValue())),
						state.getLastTime(), state.getMessage(),
						state.getLastValue() == null ? MonitorState.UNMONITOR : MonitorState.checkById(Integer.parseInt(state
								.getLastValue())));

			MonitorTask task = ServerUtil.checkTask(this, taskId);
			stateService.saveState(StateType.MONITOR_STATE, taskId, task.getModified(), MonitorState.UNMONITOR.getId(), "");
			return new MonitorRecord(taskId, MonitorState.UNMONITOR, task.getModified(), "", MonitorState.UNMONITOR);
		}
	}
	
	@Override
	public MonitorRecord[] getRecords(String taskIds) {
		StateLast[] states = stateService.getStateLasts(StateType.MONITOR_STATE, taskIds);
		if(states == null)
			return new MonitorRecord[0];
		Map<String, StateLast> map = new HashMap<String, StateLast>();
		for(StateLast state : states){
			map.put(state.getObjectId(), state);
		}
		
		List<MonitorRecord> list = new ArrayList<MonitorRecord>();
		
		String[] tasks = taskIds.split(","); 
		for(String task : tasks){
			StateLast state = map.get(task);
			if(state == null)
				continue;
			list.add(new MonitorRecord(state.getObjectId(),
				MonitorState.checkById(Integer.parseInt(state.getValue())),
				state.getLastTime(), state.getMessage(),
				state.getLastValue() == null ? MonitorState.UNMONITOR : MonitorState.checkById(Integer.parseInt(state
						.getLastValue()))));
		}

		return list.toArray(new MonitorRecord[0]);
	}

	@Override
	public void commitResults(String probeCode, MonitorResult[] results) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MonitorRecord commitResult(MonitorResult result) {
		MonitorRecord record = getRecord(result.getTaskId());
		record.set(result, record.getState());
		saveRecord(record);
		return record;
	}

	public void saveRecord(MonitorRecord record) {
		String value = Integer.toString(record.getState().getId());
		StateLast last = stateService.saveState(StateType.MONITOR_STATE, record.getTaskId(),
				record.getTime(), value, record.getMessage());

		sendMonitorAlert(record, value, last);

	}

	private void sendMonitorAlert(MonitorRecord record, String value, StateLast last) {
		try {
			if (last.getCount() == 1 || Integer.parseInt(value) == 2 || Integer.parseInt(value) == 3) {
				MonitorState lastState = last.getLastValue() == null || last.getLastValue().isEmpty() ? MonitorState.UNMONITOR
						: MonitorState.checkById(Integer.parseInt(last.getLastValue()));
				MonitorState nowState = MonitorState.checkById(Integer.parseInt(last.getValue()));
				if (lastState != MonitorState.UNMONITOR || nowState.isError()) {
					MonitorTask task = getTask(record.getTaskId());
					if (task.isEnabled()) {
						MonitorNode node = ServerUtil.checkNode(nodeService, task.getNodeId());
						String entityId = task.getResourceId();
						String entityAddr = node.getIp();
						String entityName;
						if (entityId == null) {
							entityId = task.getNodeId();
							entityName = node.getName();
						} else {
							MonitorResource res = ServerUtil.checkResource(resourceService, task.getResourceId());
							entityName = res.getName();
						}
						String methodType = null;
						String methodCode = task.getMethodCode();
						if (methodCode != null)
							methodType = ServerUtil.checkMethod(methodService, methodCode).getTypeId();
						AlertSender.sendMonitorState(task.getId(), task.getName(), entityId, entityName, entityAddr,
								nowState.getDisplayName(), lastState.getDisplayName(), record.getTime(), methodType, methodCode,
								record.getMessage());
					}
				}
			}
		} catch (Throwable e) {
			ErrorUtil.warn(logger, "发送监测任务变更消息失败：" + record, e);
		}
	}

	@Override
	public MonitorInstance[] getInstancesByTaskId(String taskId) {
		return instanceDao.getByTaskId(taskId);
	}

	@Override
	public int getTasksCountByState(MonitorState state) {
		return stateService.getStateLastCountByValue(StateType.MONITOR_STATE, state.getId());
	}

	@Override
	public int getTasksCountByProcessed() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getTasksSpeedByProcessed() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Page<MonitorTask> getTasks(PageNo pageNo, boolean currentDomain) {
		String domainId = currentDomain ? SessionManager.checkSessionDomainId() : null;
		Page<ServerSideMonitorTask> page = dao.getAll(pageNo, domainId);
		return new Page<MonitorTask>(taskIntercept(page.getRows(), currentDomain, true), page.isMore());
	}

	@Override
	public Page<MonitorTask> getTasks(PageNo pageNo) {
		Page<ServerSideMonitorTask> page = dao.getAll(pageNo);
		return new Page<MonitorTask>(taskIntercept(page.getRows(), false, true), page.isMore());
	}

	@Override
	public void dispatchTask(String taskId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MonitorResult testTask(TestParams context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTaskEnabled(String task, boolean enabled) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteTaskByNodeId(String nodeId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteTaskByResourceId(String resourceId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getMethodCodesByNodeId(String nodeId) {
		return dao.getMethodCodesByNodeId(nodeId);
	}

	@Override
	public String[] getMethodCodesByResourceId(String resourceId) {
		return dao.getMethodCodesByResourceId(resourceId);
	}

	@Override
	public String[] getTaskNodeIds() {
		return dao.getNodeIds();
	}

	@Override
	public String[] getTaskResourceIds() {
		return dao.getResourceIds();
	}

	@Override
	public void cancelCollect(String nodeId, String taskId) {
		throw new UnsupportedOperationException();

	}

	@Override
	public CollectResult getCollectResult(String nodeId, String taskId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CollectTaskSign commitTask(CollectParams arg0) {
		throw new UnsupportedOperationException();
	}

}
