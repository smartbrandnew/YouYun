package uyun.bat.web.api.metric.entity;

import java.util.HashMap;
import java.util.Map;

public class MetricTrashCleanQuery {
	private String metricName;
	private String tenantId;
	private Map<String, String> tags = new HashMap<String, String>();

	public MetricTrashCleanQuery() {

	}

	public MetricTrashCleanQuery(String metricName, String tenantId, Map<String, String> tags) {
		this.metricName = metricName;
		this.tenantId = tenantId;
		this.tags = tags;
	}

	public String getMetricName() {
		return metricName;
	}

	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}
}
