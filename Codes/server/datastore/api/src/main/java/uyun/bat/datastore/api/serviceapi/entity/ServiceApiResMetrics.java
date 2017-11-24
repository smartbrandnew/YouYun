package uyun.bat.datastore.api.serviceapi.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceApiResMetrics {
	private String resource_id;
	private Map<String, List<MetricBean>> metrics=new HashMap<String, List<MetricBean>>();

	public String getResource_id() {
		return resource_id;
	}

	public void setResource_id(String resource_id) {
		this.resource_id = resource_id;
	}

	public Map<String, List<MetricBean>> getMetrics() {
		return metrics;
	}
	public void setMetrics(Map<String, List<MetricBean>> metrics) {
		this.metrics = metrics;
	}
	@Override
	public String toString() {
		return "ServiceApiResMetrics [resource_id=" + resource_id + "]";
	}

	
	
	
	
}
