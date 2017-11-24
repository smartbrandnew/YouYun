package com.broada.carrier.monitor.client.impl.impexp;

import java.util.HashMap;
import java.util.Map;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpFile;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpNode;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpResource;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpTask;
import com.broada.carrier.monitor.client.impl.impexp.entity.Log;
import com.broada.carrier.monitor.client.impl.impexp.entity.LogLevel;
import com.broada.carrier.monitor.client.impl.impexp.util.Logger;
import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetGroup;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.service.ServerMethodService;
import com.broada.carrier.monitor.server.api.service.ServerNodeService;
import com.broada.carrier.monitor.server.api.service.ServerPolicyService;
import com.broada.carrier.monitor.server.api.service.ServerProbeService;
import com.broada.carrier.monitor.server.api.service.ServerResourceService;
import com.broada.carrier.monitor.server.api.service.ServerTargetGroupService;
import com.broada.carrier.monitor.server.api.service.ServerTaskService;

public class ExporterCarrier implements Exporter {
	private ImpExpFile file = new ImpExpFile();
	private ProcessListener exp;

	private Map<String, ImpExpNode> nodeMap = new HashMap<String, ImpExpNode>();
	private Map<String, String> resourceMap = new HashMap<String, String>();
	private Map<Integer, String> probeMap = new HashMap<Integer, String>();

	public ExporterCarrier(String ip, int port, String username, String password) {
		ServerContext.connect(ip + ":" + port);
		ServerContext.login(username, password);
	}

	public ExporterCarrier(ProcessListener exp) {
		this.exp = exp;
	}

	public ImpExpFile exp() {
		expMethods();
		expPolicies();
		expProbes();
		expNodes();
		expTasks();
		return file;
	}

	private void expMethods() {
		ServerMethodService sm = ServerContext.getMethodService();
		MonitorMethod[] monitorMethod = sm.getMethods();
		
		exp.setMaxLength(monitorMethod.length);
		exp.setMsg("正在导出监测方法...");
		for (int i = 0; i < monitorMethod.length; i++) {
			file.add(monitorMethod[i]);
			exp.setProgress(i + 1);
		}
		Logger.log(new Log(LogLevel.INFO, String.format("共导出监测方法 %d 个。", file.getMethods().size())));
	}

	private void expPolicies() {
		ServerPolicyService sp = ServerContext.getPolicyService();
		MonitorPolicy[] policys = sp.getPolicies();
		
		exp.setMaxLength(policys.length);
		exp.setMsg("正在导出监测策略...");
		for (int i = 0; i < policys.length; i++) {
			file.add(policys[i]);
			exp.setProgress(i + 1);
		}
		Logger.log(new Log(LogLevel.INFO, String.format("共导出监测策略 %d 个。", file.getPolicies().size())));
	}

	private void expProbes() {
		ServerProbeService sp = ServerContext.getProbeService();
		MonitorProbe[] probes = sp.getProbes();
		
		exp.setMaxLength(probes.length);
		exp.setMsg("正在导出监测探针...");
		for (int i = 0; i < probes.length; i++) {
			file.add(probes[i]);
			int id = probes[i].getId();
			String code = probes[i].getCode();
			probeMap.put(id, code);
			exp.setProgress(i + 1);
		}
		Logger.log(new Log(LogLevel.INFO, String.format("共导出监测探针 %d 个。", file.getProbes().size())));
	}

	private void expNodes() {
		ServerTargetGroupService st = ServerContext.getTargetGroupService();
		MonitorTargetGroup[] groups = st.getGroupsByParentId("");
		ServerNodeService sn = ServerContext.getNodeService();
		
		exp.setMaxLength(groups.length);
		for (int i = 0; i < groups.length; i++) {
			Page<MonitorNode> page = sn.getNodesByGroupId(PageNo.ALL, groups[i].getId());
			if (page.isEmpty()) {
				continue;
			} else {
				MonitorNode[] mnodes = page.getRows();
				exp.setMsg("正在导出 " + groups[i].getName() + " 节点，共" + mnodes.length + "个");
				for (int j = 0; j < mnodes.length; j++) {
					ImpExpNode node = new ImpExpNode();
					node.set(mnodes[j]);
					int probeId = node.getProbeId();
					String probeCode = probeMap.get(probeId);
					node.setProbeCode(probeCode);
					node = file.add(node);
					String nodeId = node.getId();
					expResources(nodeId, node);
					nodeMap.put(nodeId, node);
				}
			}
			exp.setProgress(i + 1);
		}
		Logger.log(new Log(LogLevel.INFO, String.format("共导出监测节点 %d 个。", file.getNodes().size())));
	}

	private void expTasks() {
		ServerTaskService st = ServerContext.getTaskService();
		
		exp.setMaxLength(nodeMap.size());
		int count = 0;
		exp.setMsg("正在导出监测任务... " + count * 100 / nodeMap.size() + "%");
		for (ImpExpNode node : nodeMap.values()) {
			MonitorTask[] mtasks = st.getTasksByNodeId(node.getId());

			for (int i = 0; i < mtasks.length; i++) {
				ImpExpTask task = new ImpExpTask();
				task.setDescription(mtasks[i].getDescription());
				task.setEnabled(mtasks[i].isEnabled());
				task.setId(mtasks[i].getId());
				task.setMethodCode(mtasks[i].getMethodCode());
				task.setModified(mtasks[i].getModified());
				task.setName(mtasks[i].getName());
				task.setNodeId(mtasks[i].getNodeId());
				task.setNodeIp(node.getIp());
				task.setParameter(mtasks[i].getParameter());
				task.setPolicyCode(mtasks[i].getPolicyCode());
				String resourceId = mtasks[i].getResourceId();
				String resourceName = resourceMap.get(resourceId);
				task.setResourceId(resourceId);
				task.setResourceName(resourceName);
				task.setTypeId(mtasks[i].getTypeId());
				file.add(task);
			}
			exp.setMsg("正在导出监测任务... " + ++count * 100 / nodeMap.size() + "%");
			exp.setProgress(count);
		}
		Logger.log(new Log(LogLevel.INFO, String.format("共导出监测任务 %d 个。", file.getTasks().size())));
	}

	private void expResources(String CarrierNodeId, ImpExpNode node) {
		if (node.getTypeId().equals("SecDev"))
			return;

		ServerResourceService srs = ServerContext.getResourceService();
		MonitorResource[] mresources = srs.getResourcesByNodeId(node.getId());

		for (int i = 0; i < mresources.length; i++) {
			ImpExpResource resource = new ImpExpResource();
			String id = mresources[i].getId();
			String name = mresources[i].getName();
			String typeId = mresources[i].getTypeId();
			resource.setName(name);
			resource.setTypeId(typeId);
			resource.setId(id);
			node.addResource(resource);
			resourceMap.put(id, name);
		}
	}

	public static void main(String[] args) {
		ExporterCarrier ec = new ExporterCarrier("127.0.0.1", 8890, "admin", "admin");
		ec.exp();
	}
}
