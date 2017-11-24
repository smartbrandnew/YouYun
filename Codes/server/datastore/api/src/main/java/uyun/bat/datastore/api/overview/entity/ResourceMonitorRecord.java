package uyun.bat.datastore.api.overview.entity;

/**
 * 租户资源监测器状态记录
 */
public class ResourceMonitorRecord {
	private String tenantId;
	private String resourceId;
	private String monitorId;
	private boolean ok;
	private boolean warn;
	private boolean error;
	private boolean info;
	private long timestamp;

	public ResourceMonitorRecord() {
		super();
	}

	public ResourceMonitorRecord(String tenantId, String resourceId, String monitorId, long timestamp) {
		super();
		this.tenantId = tenantId;
		this.resourceId = resourceId;
		this.monitorId = monitorId;
		this.timestamp = timestamp;
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

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public boolean isWarn() {
		return warn;
	}

	public void setWarn(boolean warn) {
		this.warn = warn;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public boolean isInfo() {
		return info;
	}
	
	public void setInfo(boolean info) {
		this.info = info;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
