package com.broada.carrier.monitor.server.impl.logic;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.common.util.CollectionUtil;
import com.broada.carrier.monitor.common.util.ObjectUtil;
import com.broada.carrier.monitor.common.util.SpeedCounter;
import com.broada.carrier.monitor.probe.api.client.ProbeServiceFactory;
import com.broada.carrier.monitor.probe.api.client.ProbeSync;
import com.broada.carrier.monitor.server.api.client.EventListener;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.CollectParams;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.CollectTaskSign;
import com.broada.carrier.monitor.server.api.entity.ExecuteParams;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.TestParams;
import com.broada.carrier.monitor.server.api.event.NodeChangedEvent;
import com.broada.carrier.monitor.server.api.event.ObjectChangedType;
import com.broada.carrier.monitor.server.api.event.RecordChangedEvent;
import com.broada.carrier.monitor.server.api.event.ResourceChangedEvent;
import com.broada.carrier.monitor.server.api.event.TaskChangedEvent;
import com.broada.carrier.monitor.server.api.service.ServerNodeService;
import com.broada.carrier.monitor.server.api.service.ServerResourceService;
import com.broada.carrier.monitor.server.impl.entity.ServerSideMonitorMethod;
import com.broada.carrier.monitor.server.impl.entity.ServerSideMonitorTask;
import com.broada.carrier.monitor.server.impl.event.PolicyChangedEvent;
import com.broada.carrier.monitor.server.impl.pmdb.PMDBConverter;
import com.broada.carrier.monitor.server.impl.pmdb.PMDBFacade;
import com.broada.carrier.monitor.server.impl.pmdb.map.MapInput;
import com.broada.carrier.monitor.server.impl.pmdb.map.PMDBMapper;
import com.broada.cmdb.api.model.Attribute;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.pmdb.api.model.PerfIndicatorType;
import com.broada.pmdb.api.model.StateIndicatorType;

public class ServerTaskServiceImpl implements ServerTaskServiceEx {
	private static final Logger logger = LoggerFactory.getLogger(ServerTaskServiceImpl.class);
	private static final String ITEM_CLASS = "class";
	private static final String ITEM_ATTR_PREFIX = "attr.";
	private static final String ITEM_PERF_PREFIX = "perf.";
	private static final String ITEM_STATE_PREFIX = "state.";
	@Autowired
	private ServerProbeServiceImpl probeService;
	@Autowired
	private ServerNodeService nodeService;
	@Autowired
	private ServerMethodServiceImpl methodService;
	@Autowired
	private PMDBFacade pmdbFacade;
	@Autowired
	private ServerResourceService resourceService;
	private int processedCount = 0;
	private SpeedCounter speedCounter = new SpeedCounter(30);
	private ServerTaskServiceEx target;

	public ServerTaskServiceImpl(ServerTaskServiceEx target) {
		this.target = target;
		EventBus.getDefault().registerObjectChangedListener(new TaskEventListener());
	}

	private class TaskEventListener implements EventListener {
		@Override
		public void receive(Object event) {
			if (event instanceof TaskChangedEvent)
				processTaskChanged((TaskChangedEvent) event);
			else if (event instanceof PolicyChangedEvent)
				processPolicyChanged((PolicyChangedEvent) event);
			else if (event instanceof NodeChangedEvent)
				processEvent((NodeChangedEvent) event);
			else if (event instanceof ResourceChangedEvent)
				processEvent((ResourceChangedEvent) event);
		}

		private void processEvent(ResourceChangedEvent event) {
			if (event.getType() == ObjectChangedType.DELETED)
				deleteTaskByResourceId(event.getObject().getId());
		}

		private void processEvent(NodeChangedEvent event) {
			if (event.getType() == ObjectChangedType.DELETED)
				deleteTaskByNodeId(event.getObject().getId());
		}

		private void processPolicyChanged(PolicyChangedEvent event) {
			switch (event.getType()) {
			case CREATED:
			case UPDATED:
				break;
			case DELETED:
				Page<MonitorTask> tasks = getTasksByPolicyCode(PageNo.ALL, event.getOldObject().getCode());
				for (MonitorTask task : tasks.getRows()) {
					try {
						task.setPolicyCode(MonitorPolicy.DEFAULT_POLICY_CODE);
						saveTask(task);
					} catch (Throwable e) {
						ErrorUtil.warn(logger, "修改监测任务为默认策略失败：" + task, e);
					}
				}
				break;
			}
		}

		private void processTaskChanged(TaskChangedEvent event) {
			ProbeSync sync = probeService.checkProbeSyncByTask(event.getObject());
			switch (event.getType()) {
			case CREATED:
			case UPDATED:
				try {
					sync.syncTask(event.getObject());
				} catch (Throwable e) {
					ErrorUtil.warn(logger, "同步任务到探针失败", e);
				}

				if (event.getType() == ObjectChangedType.CREATED
						&& event.getObject().getMethodCode() != null
						|| event.getType() == ObjectChangedType.UPDATED
						&& event.getOldObject() == null
						|| event.getType() == ObjectChangedType.UPDATED
						&& !ObjectUtil.equals(event.getOldObject().getMethodCode(), event.getNewObject()
								.getMethodCode())) {
					syncNodeUseMethod(event.getObject().getNodeId());
					syncResourceUseMethod(event.getObject().getResourceId());
				}
				break;
			case DELETED:
				sync.deleteTask(event.getObject());
				break;
			}
		}

		private void syncResourceUseMethod(String resourceId) {
			String[] cmdbMethodIds = pmdbFacade.getMethodIdsByTargetUsed(resourceId);
			String[] monitorMethodIds = getMethodCodesByResourceId(resourceId);
			monitorMethodIds = toMethodIds(monitorMethodIds);
			processMethodDiff(resourceId, cmdbMethodIds, monitorMethodIds);
		}

		private void syncNodeUseMethod(String nodeId) {
			String[] cmdbMethodIds = pmdbFacade.getMethodIdsByTargetUsed(nodeId);
			String[] monitorMethodIds = getMethodCodesByNodeId(nodeId);
			monitorMethodIds = toMethodIds(monitorMethodIds);
			processMethodDiff(nodeId, cmdbMethodIds, monitorMethodIds);
		}

		private String[] toMethodIds(String[] methodCodes) {
			Set<String> methodIds = new HashSet<String>();
			for (String code : methodCodes) {
				if (code == null)
					continue;
				ServerSideMonitorMethod method = methodService.getMethod(code);
				if (method == null)
					continue;
				methodIds.add(method.getId());
			}
			return methodIds.toArray(new String[methodIds.size()]);
		}

		private void processMethodDiff(String targetId, String[] cmdbMethodIds, String[] monitorMethodIds) {
			CollectionUtil.SetDiff<String> result = CollectionUtil.compareSet(cmdbMethodIds, monitorMethodIds);
			for (String methodId : result.getMores())
				pmdbFacade.deleteRelationship(targetId, PMDBConverter.TARGET_METHOD_RT, methodId);
			for (String methodId : result.getLacks())
				pmdbFacade.saveRelationship(targetId, PMDBConverter.TARGET_METHOD_RT, methodId);
		}
	}

	@Override
	public MonitorTask getTask(String taskId) {
		MonitorTask task = target.getTask(taskId);
		if (task != null) {
			boolean targetExists = false;
			if (nodeService.getNode(task.getNodeId()) == null)
				targetExists = true;
			else if (task.getResourceId() != null && resourceService.getResource(task.getResourceId()) == null)
				targetExists = true;

			if (targetExists) {
				target.deleteTask(taskId);
				task = null;
			}
		}
		return task;
	}

	@Override
	public String[] getMethodCodesByNodeId(String nodeId) {
		return target.getMethodCodesByNodeId(nodeId);
	}

	@Override
	public String[] getMethodCodesByResourceId(String resourceId) {
		return target.getMethodCodesByResourceId(resourceId);
	}

	@Override
	public Page<MonitorTask> getTasksByProbeId(PageNo pageNo, int probeId, boolean currentDomain) {
		return target.getTasksByProbeId(pageNo, probeId, currentDomain);
	}

	@Override
	public MonitorTask[] getTasksByNodeId(String nodeId) {
		return target.getTasksByNodeId(nodeId);
	}

	@Override
	public MonitorTask[] getTasksByNodeIds(String[] nodeIds) {
		return target.getTasksByNodeIds(nodeIds);
	}

	@Override
	public MonitorTask[] getTasksByResourceId(String resourceId) {
		return target.getTasksByResourceId(resourceId);
	}

	@Override
	public MonitorTask[] getTasksByResourceIds(String[] resourceIds) {
		return target.getTasksByResourceIds(resourceIds);
	}

	@Override
	public String saveTask(MonitorTask task, MonitorInstance[] instances) {
		ObjectChangedType changeType = task.getId() == null || task.getId().trim().length() <= 0 ? ObjectChangedType.CREATED
				: ObjectChangedType.UPDATED;
		ServerSideMonitorTask st = new ServerSideMonitorTask(task);
		String taskId = target.saveTask(st, instances);
		EventBus.getDefault().publishObjectChanged(new TaskChangedEvent(changeType, null, new MonitorTask(st)));
		return taskId;
	}

	@Override
	public String saveTask(MonitorTask task) {
		ObjectChangedType changeType = task.getId() == null || task.getId().trim().length() <= 0 ? ObjectChangedType.CREATED
				: ObjectChangedType.UPDATED;
		ServerSideMonitorTask st = new ServerSideMonitorTask(task);
		String taskId = target.saveTask(task);
		EventBus.getDefault().publishObjectChanged(new TaskChangedEvent(changeType, null, new MonitorTask(st)));
		return taskId;
	}

	@Override
	public void deleteTask(String taskId) {
		MonitorTask task = target.getTask(taskId);
		if (task == null)
			return;
		if (task.isEnabled() == true)
			setTaskEnabled(taskId, false);
		target.deleteTask(taskId);
		EventBus.getDefault().publishObjectChanged(
				new TaskChangedEvent(ObjectChangedType.DELETED, null, new MonitorTask(task)));
	}

	@Override
	public MonitorResult executeTask(String taskId, ExecuteParams params) {
		ProbeServiceFactory probe = probeService.checkProbeFactoryByTaskId(taskId);
		return probe.getTaskService().executeTask(taskId, params);
	}

	@Override
	public Object collectTask(CollectParams params) {
		ProbeServiceFactory probeFactory = probeService.checkProbeFactoryByNodeId(params.getNode().getId());
		Object result = probeFactory.getTaskService().collectTask(params);
		return processItem(params, result);
	}

	private Object processItem(CollectParams params, Object result) {
		if (result instanceof MonitorResult) {
			MonitorResult mr = (MonitorResult) result;
			if (mr.getItems() != null) {
				for (MonitorItem item : mr.getItems()) {
					if (item.getName() == null || item.getName().isEmpty()) {
						if (item.getCode().startsWith(ITEM_PERF_PREFIX)) {
							processPerfItem(item);
						} else if (item.getCode().startsWith(ITEM_STATE_PREFIX)) {
							processStateItem(item);
						} else {
							processAttrItem(item, params, mr);
						}
					}
				}
			}
		}
		return result;
	}

	private void processAttrItem(MonitorItem item, CollectParams params, MonitorResult mr) {
		String code = item.getCode();
		if (code.startsWith(ITEM_ATTR_PREFIX))
			code = code.substring(ITEM_ATTR_PREFIX.length());

		String cls;
		if (params.getResource() != null)
			cls = params.getResource().getTypeId();
		else
			cls = params.getNode().getTypeId();
		for (MonitorResultRow row : mr.getRows()) {
			Object value = row.getIndicator(ITEM_CLASS);
			if (value != null) {
				cls = value.toString();
				break;
			}
		}
		Attribute attr = pmdbFacade.getAttribute(cls, code);
		if (attr != null)
			item.setName(attr.getName());
	}

	private void processStateItem(MonitorItem item) {
		String[] fields = item.getCode().split("\\.");
		if (fields.length == 2) {
			String indicatorCode = fields[1];
			StateIndicatorType indicator = pmdbFacade.getStateIndicatorByCode(indicatorCode);
			if (indicator != null) {
				item.setName(indicator.getName());
				item.setDescr(indicator.getDescr());
			}
		}
	}

	private void processPerfItem(MonitorItem item) {
		String[] fields = item.getCode().split("\\.");
		if (fields.length == 3) {
			String indicatorCode = fields[2];
			PerfIndicatorType indicator = pmdbFacade.getPerfIndicatorByCode(indicatorCode);
			if (indicator != null) {
				item.setName(indicator.getName());
				item.setDescr(indicator.getDescr());
				item.setUnit(indicator.getUnit());
			}
		}
	}

	@Override
	public MonitorRecord getRecord(String taskId) {
		return target.getRecord(taskId);
	}

	@Override
	public MonitorRecord[] getRecords(String taskIds) {
		return target.getRecords(taskIds);
	}

	@Override
	public void commitResults(String probeCode, MonitorResult[] results) {
		if (logger.isDebugEnabled()) {
			logger.debug("监测结果收到，来自：" + probeCode);
			for (int i = 0; i < results.length; i++) {
				logger.debug(String.format("监测结果[%d]：%s", i + 1, results[i]));
			}
		}

		MonitorProbe probe = probeService.getProbeByCode(probeCode);
		if (probe == null) {
			logger.warn("监测结果处理放弃，此监测探针已不存在：" + probeCode);
			return;
		}

		for (MonitorResult result : results) {
			commitResult(result, probe.getId());
		}
	}

	private MonitorRecord commitResult(MonitorResult result, int probeId) {
		long start = System.currentTimeMillis();
		try {
			MonitorNode node = null;
			String error = null;
			MonitorTask task = getTask(result.getTaskId());
			if (task == null)
				error = "监测任务不存在";
			else if (probeId > 0 && !task.isEnabled())
				error = "监测任务已禁用";
			else {
				node = nodeService.getNode(task.getNodeId());
				if (node == null)
					error = "监测节点不存在";
				else if (probeId > 0 && node.getProbeId() != probeId)
					error = "此探针已不负责此监测任务";
			}

			if (error != null) {
				if (logger.isDebugEnabled())
					logger.debug("监测结果处理失败：" + error + "。监测结果：" + result);
				return null;
			}

			MonitorRecord record = target.commitResult(result);
			PMDBMapper.getDefault().process(new MapInput(resourceService, methodService, node, task, result));
			EventBus.getDefault().publishObjectChanged(new RecordChangedEvent(ObjectChangedType.CREATED, null, record));
			return record;
		} catch (Throwable e) {
			logger.warn(ErrorUtil.createMessage("监测结果处理失败", e) + "。监测结果：" + result);
			logger.debug("堆栈：", e);
			return null;
		} finally {
			long time = System.currentTimeMillis() - start;
			synchronized (speedCounter) {
				speedCounter.step(1, time);
				processedCount++;
			}
		}
	}

	@Override
	public MonitorInstance[] getInstancesByTaskId(String taskId) {
		return target.getInstancesByTaskId(taskId);
	}

	@Override
	public MonitorRecord commitResult(MonitorResult result) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getTasksCountByState(MonitorState state) {
		return target.getTasksCountByState(state);
	}

	@Override
	public int getTasksCountByProcessed() {
		return processedCount;
	}

	@Override
	public double getTasksSpeedByProcessed() {
		synchronized (speedCounter) {
			return speedCounter.getSpeed();
		}
	}

	@Override
	public Page<MonitorTask> getTasks(PageNo pageNo) {
		return target.getTasks(pageNo);
	}

	@Override
	public Page<MonitorTask> getTasks(PageNo pageNo, boolean currentDomain) {
		return target.getTasks(pageNo, currentDomain);
	}

	@Override
	public void dispatchTask(String taskId) {
		ProbeServiceFactory probe = probeService.checkProbeFactoryByTaskId(taskId);
		probe.getTaskService().dispatchTask(taskId);
	}

	@Override
	public MonitorResult testTask(TestParams params) {
		ProbeServiceFactory probe = probeService.checkProbeFactoryByNodeId(params.getNode().getId());
		return probe.getTaskService().testTask(params);
	}

	@Override
	public void setTaskEnabled(String taskId, boolean enabled) {
		MonitorTask task = ServerUtil.checkTask(this, taskId);
		if (task.isEnabled() == enabled)
			return;
		task.setEnabled(enabled);
		MonitorInstance[] instances = getInstancesByTaskId(taskId);
		saveTask(task, instances);

		if (!task.isEnabled()) {
			MonitorResult mr = new MonitorResult();
			mr.setTaskId(task.getId());
			mr.setState(MonitorState.UNMONITOR);
			mr.setMessage("监测任务被停用");
			commitResult(mr, 0);
		}
	}

	@Override
	public void deleteTaskByNodeId(String nodeId) {
		MonitorTask[] tasks = getTasksByNodeId(nodeId);
		for (MonitorTask task : tasks)
			deleteTask(task.getId());
	}

	@Override
	public void deleteTaskByResourceId(String resourceId) {
		MonitorTask[] tasks = getTasksByResourceId(resourceId);
		for (MonitorTask task : tasks)
			deleteTask(task.getId());
	}

	@Override
	public Page<MonitorTask> getTasksByPolicyCode(PageNo pageNo, String policyCode) {
		return target.getTasksByPolicyCode(pageNo, policyCode);
	}

	@Override
	public String[] getTaskNodeIds() {
		return target.getTaskNodeIds();
	}

	@Override
	public String[] getTaskResourceIds() {
		return target.getTaskResourceIds();
	}

	@Override
	public int getLicenseUsedQuotaPCServer() {
		return target.getLicenseUsedQuotaPCServer();
	}

	@Override
	public int getLicenseUsedQuotaMiniServer() {
		return target.getLicenseUsedQuotaMiniServer();
	}

	@Override
	public int getLicenseUsedQuotaStorageDev() {
		return target.getLicenseUsedQuotaStorageDev();
	}

	@Override
	public int getLicenseUsedQuotaAppPlatform() {
		return target.getLicenseUsedQuotaAppPlatform();
	}

	@Override
	public void cancelCollect(String nodeId, String taskId) {
		ProbeServiceFactory probeFactory = probeService.checkProbeFactoryByNodeId(nodeId);
		probeFactory.getTaskService().cancelCollect(nodeId, taskId);

	}

	@Override
	public CollectResult getCollectResult(String nodeId, String taskId) {
		ProbeServiceFactory probeFactory = probeService.checkProbeFactoryByNodeId(nodeId);
		return probeFactory.getTaskService().getCollectResult(nodeId, taskId);
	}

	@Override
	public CollectTaskSign commitTask(CollectParams params) {
		ProbeServiceFactory probeFactory = probeService.checkProbeFactoryByNodeId(params.getNode().getId());
		return probeFactory.getTaskService().commitTask(params);
	}
}
