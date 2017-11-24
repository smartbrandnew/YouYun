package uyun.bat.dashboard.api.entity;

import java.util.List;

public class TenantResTemplate {
	private String dashboardId;
	private String resourceId;
	private String tenantId;
	private String appName;
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public String getDashboardId() {
		return dashboardId;
	}
	public void setDashId(String dashboardId) {
		this.dashboardId = dashboardId;
	}
}
