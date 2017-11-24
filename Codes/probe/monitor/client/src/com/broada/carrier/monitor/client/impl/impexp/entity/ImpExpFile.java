package com.broada.carrier.monitor.client.impl.impexp.entity;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.client.impl.impexp.util.Logger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;

public class ImpExpFile {
	private List<MonitorMethod> methods = new LinkedList<MonitorMethod>();
	private List<MonitorPolicy> policies = new LinkedList<MonitorPolicy>();
	private Map<String, MonitorProbe> probes = new LinkedHashMap<String, MonitorProbe>();
	private Map<String, ImpExpNode> nodes = new LinkedHashMap<String, ImpExpNode>();
	private List<ImpExpTask> tasks = new LinkedList<ImpExpTask>();

	public List<MonitorMethod> getMethods() {
		return methods;
	}

	public List<MonitorPolicy> getPolicies() {
		return policies;
	}

	public Map<String, MonitorProbe> getProbes() {
		return probes;
	}

	public Map<String, ImpExpNode> getNodes() {
		return nodes;
	}

	public List<ImpExpTask> getTasks() {
		return tasks;
	}

	public void add(MonitorProbe probe) {
		if (probes.containsKey(probe.getCode()))
			Logger.log(new Log(LogLevel.WARN, "监测探针编码重复，将优先使用第1个：" + probe.getCode()));
		else
			probes.put(probe.getCode(), probe);
	}

	public void add(ImpExpTask task) {
		tasks.add(task);
	}

	public void add(String ip, ImpExpResource resource) {
		ImpExpNode node = nodes.get(ip);
		if (node == null)
			Logger.log(new Log(LogLevel.WARN, String.format("监测节点[%s]不存在，此资源将忽略：%s", ip, resource.getName())));
		else
			node.addResource(resource);
	}

	public ImpExpNode add(ImpExpNode node) {
		ImpExpNode exists = nodes.get(node.getIp());
		if (exists == null) {
			nodes.put(node.getIp(), node);
			exists = node;
		} else
			Logger.log(new Log(LogLevel.WARN, "监测节点IP地址重复，将优先使用第1个：" + node.getIp()));
		return exists;
	}

	public void add(MonitorPolicy policy) {
		policies.add(policy);
	}

	public void add(MonitorMethod method) {
		methods.add(method);
	}

	public MonitorProbe checkProbe(String probeCode) {
		MonitorProbe probe = probes.get(probeCode);
		if (probe == null)
			probe = new MonitorProbe();
		return probe;
	}

	public ImpExpNode checkNode(String nodeIp) {
		ImpExpNode node = nodes.get(nodeIp);
		if (node == null)
			throw new IllegalArgumentException("未知的监测节点：" + nodeIp);
		return node;
	}

	public MonitorMethod find(MonitorMethod method) {
		for (MonitorMethod item : methods) {
			if (method.equalsData(item))
				return item;
		}
		return null;
	}

	public MonitorPolicy find(MonitorPolicy policy) {
		for (MonitorPolicy item : policies) {
			if (policy.equalsData(item))
				return item;
		}
		return null;
	}
}
