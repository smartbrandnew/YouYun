package com.broada.carrier.monitor.client.impl.impexp;

import java.util.HashSet;
import java.util.Set;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.client.impl.cache.ClientCache;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpFile;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpNode;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpResource;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpTask;
import com.broada.carrier.monitor.client.impl.impexp.entity.Log;
import com.broada.carrier.monitor.client.impl.impexp.entity.LogLevel;
import com.broada.carrier.monitor.client.impl.impexp.util.Logger;
import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.MonitorType;

public class Importer {
	private String serverIp;
	private String username;
	private String password;
	private ImpExpFile file;
	private ProcessListener imp;
	private boolean alwaysDisableTasks = true;

	public Importer(String serverIp, String username, String password,
			ImpExpFile file) {
		this.serverIp = serverIp;
		this.username = username;
		this.password = password;
		this.file = file;
	}

	public void imp() {
		ServerContext.connect(serverIp);
		ServerContext.login(username, password);
		try {
			impMethods();
			impPolicies();
			impProbes();
			impNodes();
			impTasks();
			syncTasks();
		} finally {
			ServerContext.logout();
		}
	}

	public Importer(ImpExpFile file, ProcessListener imp) {
		this.file = file;
		this.imp = imp;
	}

	public void impClient() {
		impMethods();
		impPolicies();
		impProbes();
		impNodes();
		impTasks();
		syncTasks();
	}

	private void impTasks() {
		imp.setMaxLength(file.getTasks().size());
		int i = 0;
		imp.setMsg("正在导入监测任务... " + i * 100 / file.getTasks().size() + "%");
		Set<String> existsTaskTypes = new HashSet<String>();
		for (ImpExpTask task : file.getTasks()) {
			try {
				if (!existsTaskTypes.contains(task.getTypeId())) {
					MonitorType type = ServerContext.getTypeService().getType(
							task.getTypeId());
					if (type == null) {
						Logger.log(new Log(LogLevel.WARN, "监测任务类型"
								+ task.getTypeId() + "不支持，将不提交监测任务："
								+ task.getDisplayName()));
						continue;
					}
					existsTaskTypes.add(task.getTypeId());
				}

				if (task.getMethodCode() == "" || task.getMethodCode().trim().length() == 0)
					task.setMethodCode(null);
				ImpExpNode node = file.checkNode(task.getNodeIp());
				MonitorResource resource = node.checkResource(task
						.getResourceName());
				task.setNodeId(node.getId());

				MonitorTask[] tasks;
				if (resource != null) {
					task.setResourceId(resource.getId());
					tasks = ServerContext.getTaskService()
							.getTasksByResourceId(resource.getId());
				} else
					tasks = ServerContext.getTaskService().getTasksByNodeId(
							node.getId());

				if (alwaysDisableTasks)
					task.setEnabled(false);

				for (MonitorTask mt : tasks) {
					if (mt.getName().equalsIgnoreCase(task.getName())) {
						task.setId(mt.getId());
						break;
					}
				}
				ServerContext.getTaskService().saveTask(task,
						task.getInstances().toArray(new MonitorInstance[0]));
				Logger.log(new Log(LogLevel.INFO, "监测任务 提交成功："
						+ task.getDisplayName()));
			} catch (Throwable e) {
				Logger.log(new Log(LogLevel.WARN,
						"监测任务 提交失败：" + task.getDisplayName(), e));
			}
			imp.setMsg("正在导入监测任务... " + ++i * 100 / file.getTasks().size() + "%");
			imp.setProgress(i);
		}
	}

	private void impNodes() {
		imp.setMaxLength(file.getNodes().values().size());
		imp.setMsg("正在导入监测节点...");
		int i = 0;
		for (ImpExpNode node : file.getNodes().values()) {
			try {
				MonitorProbe probe = file.checkProbe(node.getProbeCode());
				node.setProbeId(probe.getId());

				Page<MonitorNode> nodes = ServerContext.getNodeService()
						.getNodesByIp(PageNo.ONE, node.getIp());
				String nodeId = null;
				if (nodes != null && nodes.getRowsCount() > 0) {
					for (MonitorNode item : nodes.getRows())
						if (item.getIp().equals(node.getIp())) {
							nodeId = item.getId();
							break;
						}
				}
				node.setId(nodeId);
				nodeId = ServerContext.getNodeService().saveNode(node);
				node.setId(nodeId);
				Logger.log(new Log(LogLevel.INFO, "监测节点 提交成功：" + node.getIp()));
				impResources(node);
			} catch (Throwable e) {
				Logger.log(new Log(LogLevel.WARN, "监测节点 提交失败：" + node.getIp(),
						e));
			}
			imp.setProgress(++i);
		}
	}

	private void impResources(ImpExpNode node) {
		for (ImpExpResource resource : node.getResources().values()) {
			try {
				resource.setNodeId(node.getId());
				String resourceId = ServerContext.getResourceService()
						.saveResource(resource);
				resource.setId(resourceId);
				Logger.log(new Log(LogLevel.INFO, "监测资源 提交成功：" + node.getIp()
						+ "-" + resource.getName()));
			} catch (Throwable e) {
				Logger.log(new Log(LogLevel.WARN, "监测资源 提交失败：" + node.getIp()
						+ "-" + resource.getName(), e));
			}
		}
	}

	private void impProbes() {
		imp.setMaxLength(file.getProbes().values().size());
		imp.setMsg("正在导入监测探针...");
		int i = 0;
		for (MonitorProbe probe : file.getProbes().values()) {
			try {
				int probeId = ServerContext.getProbeService().saveProbe(probe);
				probe.setId(probeId);
				Logger.log(new Log(LogLevel.INFO, "监测探针 提交成功："
						+ probe.getCode()));
			} catch (Throwable e) {
				Logger.log(new Log(LogLevel.WARN, "监测探针 提交失败："
						+ probe.getCode(), e));
			}
			imp.setProgress(++i);
		}
	}

	private void impPolicies() {
		imp.setMaxLength(file.getPolicies().size());
		imp.setMsg("正在导入监测策略...");
		int i = 0;
		for (MonitorPolicy policy : file.getPolicies()) {
			try {
				ServerContext.getPolicyService().savePolicy(policy);
				Logger.log(new Log(LogLevel.INFO, "监测策略 提交成功："
						+ policy.getCode()));
			} catch (Throwable e) {
				Logger.log(new Log(LogLevel.WARN, "监测策略 提交失败："
						+ policy.getCode(), e));
			}
			imp.setProgress(++i);
		}
	}

	private void impMethods() {
		imp.setMaxLength(file.getMethods().size());
		imp.setMsg("正在导入监测协议...");
		int i = 0;
		for (MonitorMethod method : file.getMethods()) {
			try {
				ServerContext.getMethodService().saveMethod(method);
				Logger.log(new Log(LogLevel.INFO, "监测方法 提交成功："
						+ method.getCode()));
			} catch (Throwable e) {
				Logger.log(new Log(LogLevel.WARN, "监测方法 提交失败："
						+ method.getCode(), e));
			}
			imp.setProgress(++i);
		}
	}

	private void syncTasks() {
		for (MonitorProbe probe : file.getProbes().values()) {
			String probeCode = probe.getCode();
			if (probeCode == null)
				continue;
			MonitorProbe mprobe = ServerContext.getProbeService().getProbeByCode(probeCode);
			ServerContext.getProbeService().syncProbe(mprobe.getId());
		}
		ClientCache.reloadCache();
	}
}
