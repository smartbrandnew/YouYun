package com.broada.carrier.monitor.probe.impl.yaml;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.api.client.ProbeServiceFactory;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;

public class YamlTaskService {
	private static final Logger logger = LoggerFactory.getLogger(YamlTaskService.class);
	@Autowired
	private ProbeServiceFactory probeFactory;

	public void syncAll() {
		deleteExpireTasks();
		deleteOldNodes();
		deleteOldMethods();
		deleteOldPolicys();

		saveMonitorNode();
		saveMonitorMethod();
		saveMonitorPolicy();
		saveMonitorTask();
	}

	public long saveMonitorTask() {
		List<MonitorTask> list = YamlUtil.getInstance().getAllMonitorTask();
		for (MonitorTask task : list) {
			probeFactory.getTaskService().saveTask(task, null, new MonitorRecord(task.getId()));
			logger.info("保存监测任务信息:{}", task);
		}
		logger.info("更新Yaml Monitor Tasks，更新Task数量：{}个", list.size());
		return list.size();
	}

	public long saveMonitorNode() {
		List<MonitorNode> list = YamlUtil.getInstance().getAllMonitorNode();
		for (MonitorNode node : list) {
			probeFactory.getNodeService().saveNode(node);
		}
		logger.info("更新Yaml Monitor Nodes，更新Node数量：{}个", list.size());
		return list.size();
	}

	public long saveMonitorMethod() {
		List<MonitorMethod> list = YamlUtil.getInstance().getAllMonitorMethod();
		for (MonitorMethod method : list) {
			probeFactory.getMethodService().saveMethod(method);
		}
		logger.info("更新Yaml Monitor Methods，更新Method数量：{}个", list.size());
		return list.size();
	}

	public long saveMonitorPolicy() {
		List<MonitorPolicy> list = YamlUtil.getInstance().getAllMonitorPolicy();
		for (MonitorPolicy policy : list) {
			probeFactory.getPolicyService().savePolicy(policy);
		}
		logger.info("更新Yaml Policys，更新Policy数量：{}个", list.size());
		return list.size();
	}

	public void deleteExpireTasks() {
		List<String> list = probeFactory.getTaskService().getAllTaskIds();
		logger.info("数据库中原有任务ID信息: {}", list);
		List<String> updateList = YamlUtil.getInstance().getUpdateTaskIds();
		logger.info("本次更新任务ID信息: {}", updateList);
		list.remove(updateList);
		for (String id : list) {
			probeFactory.getTaskService().delete(id);
		}
	}

	public void deleteOldNodes() {
		MonitorNode[] nodes = probeFactory.getNodeService().getNodes();
		if (nodes != null) {
			for (MonitorNode node : nodes) {
				String id = node.getId();
				if (id != null)
					probeFactory.getNodeService().deleteNode(id);
			}
		}
	}

	public void deleteOldMethods() {
		MonitorMethod[] methods = probeFactory.getMethodService().getMethods();
		if (methods != null) {
			for (MonitorMethod method : methods) {
				String methodCode = method.getCode();
				if (methodCode != null)
					probeFactory.getMethodService().deleteMethod(methodCode);
			}
		}
	}

	public void deleteOldPolicys() {
		MonitorPolicy[] policys = probeFactory.getPolicyService().getPolicies();
		for (MonitorPolicy policy : policys) {
			String id = policy.getCode();
			if (id != null)
				probeFactory.getPolicyService().deletePolicy(id);
		}
	}
}
