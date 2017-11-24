package uyun.bat.monitor.api.mq;

import java.io.Serializable;

public class MonitorEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private String monitorId;
	private String tenantId;

	public MonitorEvent() {
		super();
	}

	public MonitorEvent(String monitorId, String tenantId) {
		super();
		this.monitorId = monitorId;
		this.tenantId = tenantId;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

}
