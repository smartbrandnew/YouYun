package uyun.bat.datastore.api.entity;

import java.io.Serializable;

public class ResourceApp implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String appName;
	private String tenantId;

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public ResourceApp() {

	}

	public ResourceApp(String id, String tenantId, String appName) {
		this.id = id;
		this.tenantId = tenantId;
		this.appName = appName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	@Override
	public String toString() {
		return "ResourceApp [id=" + id + ", appName=" + appName + ", tenantId=" + tenantId + "]";
	}


}
