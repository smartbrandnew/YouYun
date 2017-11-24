package com.broada.carrier.monitor.probe.impl.yaml;

import java.util.List;
import java.util.Map;

public class YamlBean {
	private List<YamlHost> hosts;
	private List<Map<String, Object>> collect_methods;

	public List<YamlHost> getHosts() {
		return hosts;
	}

	public void setHosts(List<YamlHost> hosts) {
		this.hosts = hosts;
	}

	public List<Map<String, Object>> getCollect_methods() {
		return collect_methods;
	}

	public void setCollect_methods(List<Map<String, Object>> collect_methods) {
		this.collect_methods = collect_methods;
	}

	@Override
	public String toString() {
		return "YamlBean [hosts=" + hosts + ", collect_methods=" + collect_methods + "]";
	}
}
