package com.broada.carrier.monitor.probe.impl.logic;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.probe.api.client.ProbeServiceFactory;
import com.broada.carrier.monitor.probe.api.client.ProbeUtil;
import com.broada.carrier.monitor.probe.impl.dispatch.MonitorDispatcher;
import com.broada.carrier.monitor.probe.impl.dispatch.WorkDispatcher;
import com.broada.carrier.monitor.probe.impl.dispatch.WorkItem;
import com.broada.carrier.monitor.probe.impl.dispatch.WorkType;
import com.broada.carrier.monitor.probe.impl.entity.MonitorResultCache;
import com.broada.carrier.monitor.probe.impl.entity.ProbeSideMonitorContext;
import com.broada.carrier.monitor.server.api.entity.CollectParams;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.CollectTaskSign;
import com.broada.carrier.monitor.server.api.entity.ExecuteParams;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.TestParams;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.carrier.monitor.spi.entity.MonitorTempData;
import com.broada.component.utils.error.ErrorUtil;

public class ProbeTaskServiceImpl implements ProbeTaskServiceEx {
	private static final Logger logger = LoggerFactory
			.getLogger(ProbeTaskServiceImpl.class);
	@Autowired
	private ProbeServiceFactory probeFactory;
	private ProbeTaskServiceEx target;
	private TaskListener taskListener;

	public ProbeTaskServiceImpl(ProbeTaskServiceEx target) {
		this.target = target;
	}

	@Override
	public void saveTask(MonitorTask task, MonitorInstance[] instances,
			MonitorRecord record) {
		MonitorTask exists = getTask(task.getId());
		target.saveTask(task, instances, record);
		if (taskListener != null) {
			try {
				if (exists == null)
					taskListener.onCreated(task);
				else
					taskListener.onChanged(task);
			} catch (Throwable e) {
				ErrorUtil.warn(logger, "通知任务保存失败", e);
			}
		}
	}

	@Override
	public void deleteTask(String taskId) {
		MonitorTask task = getTask(taskId);
		if (task == null)
			return;

		target.deleteTask(taskId);
		if (taskListener != null) {
			try {
				taskListener.onDelete(task);
			} catch (Throwable e) {
				ErrorUtil.warn(logger, "通知任务删除失败", e);
			}
		}
	}

	@Override
	public MonitorResult executeTask(String taskId, ExecuteParams params) {
		MonitorTask task = ProbeUtil.checkTask(this, taskId);
		MonitorNode node = ProbeUtil.checkNode(probeFactory.getNodeService(),
				task.getNodeId());
		MonitorResource resource = null;
		if (task.getResourceId() != null)
			resource = ProbeUtil.checkResource(
					probeFactory.getResourceService(), task.getResourceId());
		MonitorMethod method = null;
		if (task.getMethodCode() != null)
			method = ProbeUtil.checkMethod(probeFactory.getMethodService(),
					task.getMethodCode());
		MonitorInstance[] instances = getInstancesByTaskId(taskId);
		MonitorPolicy policy = probeFactory.getPolicyService().getPolicy(
				task.getPolicyCode());
		MonitorRecord record = getRecord(taskId);
		MonitorContext context = new ProbeSideMonitorContext(node, resource,
				method, task, instances, this, policy, record);
		return (MonitorResult) WorkDispatcher.getDefault().executeWork(
				new WorkItem(WorkType.EXECUTE, params.getTimeout(), context));
	}

	@Override
	public Serializable collectTask(CollectParams params) {
		return (Serializable) WorkDispatcher.getDefault().executeWork(
				new WorkItem(WorkType.COLLECT, 2 * 60 * 1000, new Object[] {
						params, params.getTypeId() }));
	}

	@Override
	public MonitorRecord getRecord(String taskId) {
		return target.getRecord(taskId);
	}

	@Override
	public void saveRecord(MonitorRecord record) {
		target.saveRecord(record);
	}

	@Override
	public MonitorTask getTask(String taskId) {
		return target.getTask(taskId);
	}

	public void setListener(TaskListener taskListener) {
		this.taskListener = taskListener;
	}

	@Override
	public MonitorTask[] getTasks() {
		return target.getTasks();
	}

	@Override
	public void deleteAll() {
		target.deleteAll();
	}

	@Override
	public MonitorTask[] getTasksByPolicyCode(String policyCode) {
		return target.getTasksByPolicyCode(policyCode);
	}

	@Override
	public int getResultCachesCount() {
		return target.getResultCachesCount();
	}

	@Override
	public MonitorResultCache[] getResultCaches(PageNo oageNo) {
		return target.getResultCaches(oageNo);
	}

	@Override
	public void deleteResultCaches(MonitorResultCache[] caches) {
		target.deleteResultCaches(caches);
	}

	@Override
	public void saveResultCache(MonitorResultCache cache) {
		target.saveResultCache(cache);
	}

	@Override
	public MonitorInstance[] getInstancesByTaskId(String taskId) {
		return target.getInstancesByTaskId(taskId);
	}

	@Override
	public void dispatchTask(String taskId) {
		MonitorDispatcher.getDefault().dispatchTask(taskId);
	}

	@Override
	public MonitorResult testTask(TestParams params) {
		return (MonitorResult) WorkDispatcher.getDefault().executeWork(
				new WorkItem(WorkType.TEST, 30000, params));
	}

	@Override
	public void saveTempData(MonitorTempData tempData) {
		target.saveTempData(tempData);
	}

	@Override
	public MonitorTempData getTempData(String taskId) {
		return target.getTempData(taskId);
	}

	@Override
	public void cancelCollect(String nodeId, String taskId) {
		WorkDispatcher.getDefault().cancelCollect(taskId);

	}

	@Override
	public CollectResult getCollectResult(String nodeId, String taskId) {
		return WorkDispatcher.getDefault().getCollectResult(taskId);
	}

	@Override
	public CollectTaskSign commitTask(CollectParams params) {
		CollectResult result = new CollectResult();
		return WorkDispatcher.getDefault().commitWork(
				new WorkItem(WorkType.COLLECT, 10 * 60 * 1000, new Object[] {
						params, params.getTypeId(), result }));
	}

	@Override
	public List<String> getAllTaskIds() {
		return target.getAllTaskIds();
	}

	@Override
	public void delete(String id) {
		target.delete(id);

	}

}
