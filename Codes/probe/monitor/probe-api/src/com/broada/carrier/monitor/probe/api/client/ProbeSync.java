package com.broada.carrier.monitor.probe.api.client;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.error.TargetNotExistsException;
import com.broada.component.utils.text.Unit;

public class ProbeSync {
	private static final Logger logger = LoggerFactory.getLogger(ProbeSync.class);
	private MonitorProbe probe;
	private ProbeServiceFactory probeFactory;
	private ServerServiceFactory serverFactory;
	private Map<String, MonitorNode> nodes;
	private Map<String, MonitorResource> resources;
	private Map<String, MonitorMethod> methods;
	private Map<String, MonitorPolicy> policies;
	private Counter nodesCounter = new Counter("节点");
	private Counter resourcesCounter = new Counter("资源");
	private Counter tasksCounter = new Counter("任务");
	private Counter methodsCounter = new Counter("方法");
	private Counter policiesCounter = new Counter("策略");
	private Counter[] counters = new Counter[] { nodesCounter, resourcesCounter, tasksCounter, methodsCounter,
			policiesCounter };
	private int lastProgress;
	private String lastMessage;

	public MonitorProbe getProbe() {
		return probe;
	}

	private static class Counter {
		private String name;
		private int created;
		private int updated;
		private int deleted;
		private int same;

		public Counter(String name) {
			this.name = name;
		}

		public void reset() {
			created = 0;
			updated = 0;
			deleted = 0;
			same = 0;
		}

		public void getReport(StringBuilder sb) {
			sb.append(name);
			sb.append("\t不变：").append(same);
			sb.append("\t新增：").append(created);
			sb.append("\t更新：").append(updated);
			sb.append("\t删除：").append(deleted);
		}
	}

	private void fire(int progress, String message) {
		lastProgress = progress;
		lastMessage = message;
	}

	// TODO 2015-02-16 09:40:16 增加node等时间戳后实现增量同步
	public ProbeSync(MonitorProbe probe, ProbeServiceFactory probeFactory, ServerServiceFactory serverFactory) {
		this.probe = probe;
		this.probeFactory = probeFactory;
		this.serverFactory = serverFactory;
	}

	public int getLastProgress() {
		return lastProgress;
	}

	public String getLastMessage() {
		return lastMessage;
	}

	public void syncAll(boolean deleteAll) {
		long start = System.currentTimeMillis();
		logger.debug("探针同步开始：{}", probe);
		methods = null;
		policies = null;
		for (Counter counter : counters)
			counter.reset();

		fire(0, "正在清理探针数据");
		if (deleteAll) {
			logger.debug("探针同步 - 删除探针所有当前数据：{}", probe);
			probeFactory.getSystemService().deleteAll();
		}

		syncTasks();

		long time = System.currentTimeMillis() - start;
		StringBuilder sb = new StringBuilder();
		for (Counter counter : counters) {
			if (sb.length() > 0)
				sb.append("\n");
			counter.getReport(sb);
		}
		fire(100, "探针同步完成");
		logger.info("探针同步完成：{} 共耗时：{} 统计：\n" + sb, probe, Unit.ms.formatPrefer(time));
	}

	public void deleteNode(MonitorNode probeNode) {
		logger.debug("探针同步 - 删除节点：{}", probeNode);
		probeFactory.getNodeService().deleteNode(probeNode.getId());
	}

	private void syncTasks() {
		MonitorTask[] serverTasks = serverFactory.getTaskService().getTasksByProbeId(PageNo.ALL, probe.getId(), false)
				.getRows();
		MonitorTask[] probeTasks = probeFactory.getTaskService().getTasks();

		int count = 0;
		int progress = -1;
		for (MonitorTask serverTask : serverTasks) {
			int temp = (int) (count * 100.0 / serverTasks.length);
			if (temp != progress)
				fire(temp, "正在同步监测任务：" + serverTask);

			MonitorTask probeTask = null;
			for (MonitorTask task : probeTasks) {
				if (serverTask.getId() == task.getId()) {
					probeTask = task;
					break;
				}
			}

			if (probeTask == null) {
				if (syncTask(serverTask))
					tasksCounter.created++;
			} else if (probeTask.getModified() != serverTask.getModified()) {
				if (syncTask(serverTask))
					tasksCounter.updated++;
			} else {
				syncTaskDepends(serverTask);
				tasksCounter.same++;
			}
			count++;
		}

		for (MonitorTask probeTask : probeTasks) {
			MonitorTask serverTask = null;
			for (MonitorTask task : serverTasks) {
				if (probeTask.getId() == task.getId()) {
					serverTask = task;
					break;
				}
			}

			if (serverTask == null) {
				deleteTask(probeTask);
				tasksCounter.deleted++;
			}
		}
	}

	public void deleteTask(MonitorTask task) {
		logger.debug("探针同步 - 删除任务：{}", task);
		probeFactory.getTaskService().deleteTask(task.getId());
	}

	public void deleteResource(MonitorResource resource) {
		logger.debug("探针同步 - 删除资源：{}", resource);
		probeFactory.getResourceService().deleteResource(resource.getId());
	}

	public boolean syncTask(MonitorTask task) {
		try {
			syncTaskDepends(task);

			logger.debug("探针同步 - 下发任务：{}", task);
			probeFactory.getTaskService().saveTask(task, serverFactory.getTaskService().getInstancesByTaskId(task.getId()),
					serverFactory.getTaskService().getRecord(task.getId()));
			return true;
		} catch (TargetNotExistsException e) {
			logger.debug(String.format("探针同步 - 下发任务取消，相关监测项已不存在：%s", task), e);
			return false;
		}
	}

	public void syncTaskDepends(MonitorTask task) {
		syncTaskDepends(task.getNodeId(), task.getResourceId(), task.getMethodCode(), task.getPolicyCode());
	}

	public void syncTaskDepends(String nodeId, String resourceId, String methodCode, String policyCode) {
		syncNode(nodeId);
		syncResource(resourceId);
		syncMethod(methodCode);
		syncPolicy(policyCode);
	}

	public void syncNode(String nodeId) {
		if (nodes == null)
			nodes = new HashMap<String, MonitorNode>();

		MonitorNode probeNode = nodes.get(nodeId);
		if (probeNode != null)
			return;

		probeNode = probeFactory.getNodeService().getNode(nodeId);
		if (probeNode != null)
			nodes.put(nodeId, probeNode);

		MonitorNode serverNode = ServerUtil.checkNode(serverFactory.getNodeService(), nodeId);
		if (probeNode == null) {
			if (logger.isDebugEnabled())
				logger.debug("探针同步 - 下发节点新增：{} 线程：{}", serverNode, Thread.currentThread().getId());
			probeFactory.getNodeService().saveNode(serverNode);
			nodes.put(nodeId, serverNode);
			nodesCounter.created++;
		} else if (probeNode.getModified() != serverNode.getModified()) {
			if (logger.isDebugEnabled())
				logger.debug("探针同步 - 下发节点修改：{} 线程：{}", serverNode, Thread.currentThread().getId());
			probeFactory.getNodeService().saveNode(serverNode);
			nodesCounter.updated++;
		} else
			nodesCounter.same++;
	}

	public void syncResource(String resourceId) {
		if (resourceId == null)
			return;

		if (resources == null)
			resources = new HashMap<String, MonitorResource>();

		MonitorResource probeResource = resources.get(resourceId);
		if (probeResource != null)
			return;

		probeResource = probeFactory.getResourceService().getResource(resourceId);
		if (probeResource != null)
			resources.put(resourceId, probeResource);

		MonitorResource serverResource = ServerUtil.checkResource(serverFactory.getResourceService(), resourceId);
		syncNode(serverResource.getNodeId());
		if (probeResource == null) {
			logger.debug("探针同步 - 下发资源：{}", serverResource);
			probeFactory.getResourceService().saveResource(serverResource);
			resources.put(resourceId, serverResource);
			resourcesCounter.created++;
		} else if (probeResource.getModified() != serverResource.getModified()) {
			logger.debug("探针同步 - 下发资源：{}", serverResource);
			probeFactory.getResourceService().saveResource(serverResource);
			resourcesCounter.updated++;
		} else
			resourcesCounter.same++;
	}

	public void syncPolicy(String policyCode) {
		if (policyCode == null)
			return;

		if (policies == null)
			policies = new HashMap<String, MonitorPolicy>();

		MonitorPolicy probePolicy = policies.get(policyCode);
		if (probePolicy != null)
			return;

		probePolicy = probeFactory.getPolicyService().getPolicy(policyCode);
		if (probePolicy != null)
			policies.put(policyCode, probePolicy);

		MonitorPolicy serverPolicy = ServerUtil.checkPolicy(serverFactory.getPolicyService(), policyCode);
		if (probePolicy == null) {
			logger.debug("探针同步 - 下发策略：{}", serverPolicy);
			probeFactory.getPolicyService().savePolicy(serverPolicy);
			policies.put(policyCode, serverPolicy);
			policiesCounter.created++;
		} else if (probePolicy.getModified() != serverPolicy.getModified()) {
			logger.debug("探针同步 - 下发策略：{}", serverPolicy);
			probeFactory.getPolicyService().savePolicy(serverPolicy);
			policiesCounter.updated++;
		} else
			policiesCounter.same++;
	}

	public void syncMethod(String methodCode) {
		if (methodCode == null || methodCode == "" || methodCode.length() == 0)
			return;

		if (methods == null)
			methods = new HashMap<String, MonitorMethod>();

		MonitorMethod probeMethod = methods.get(methodCode);
		if (probeMethod != null)
			return;

		probeMethod = probeFactory.getMethodService().getMethod(methodCode);
		if (probeMethod != null)
			methods.put(methodCode, probeMethod);

		MonitorMethod serverMethod = serverFactory.getMethodService().getMethod(methodCode);
		if (probeMethod == null && serverMethod == null) {
			return;
		}
		if (probeMethod == null && serverMethod != null) {
			logger.debug("探针同步 - 下发方法：{}", serverMethod);
			probeFactory.getMethodService().saveMethod(serverMethod);
			methods.put(methodCode, serverMethod);
			methodsCounter.created++;
		} else if (probeMethod != null && serverMethod == null) {
			logger.debug("探针同步 - 删除方法：{}", probeMethod);
			probeFactory.getMethodService().deleteMethod(methodCode);
			methods.put(methodCode, serverMethod);
			methodsCounter.deleted++;
		} else if (probeMethod.getModified() != serverMethod.getModified()) {
			logger.debug("探针同步 - 下发方法：{}", serverMethod);
			probeFactory.getMethodService().saveMethod(serverMethod);
			methodsCounter.updated++;
		} else
			methodsCounter.same++;
	}

	public void deleteMethod(String methodCode) {
		logger.debug("探针同步 - 删除方法：{}", methodCode);
		probeFactory.getMethodService().deleteMethod(methodCode);
	}
}
