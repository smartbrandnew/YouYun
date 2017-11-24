package uyun.bat.datastore.entity;

import java.util.ArrayList;
import java.util.List;

public class ResourceMetrtics {
	private String resourceId;
	private List<String> metricNames = new ArrayList<String>();
	private String tenantId;

	public ResourceMetrtics() {

	}
	
	public ResourceMetrtics(String resourceId, List<String> metricNames, String tenantId) {
		this.resourceId = resourceId;
		this.metricNames = metricNames;
		this.tenantId = tenantId;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public List<String> getMetricNames() {
		return metricNames;
	}

	public void setMetricNames(List<String> metricNames) {
		this.metricNames = metricNames;
	}

}
