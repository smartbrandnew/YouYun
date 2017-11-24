package uyun.bat.monitor.api.entity;

import java.util.Date;

public class AutoRecoverRecord {
	private String id;
	private String monitorId;
	private String monitorName;
	private String resId;
	private String hostName;
	private Date time;
	private String tenantId;
	private String executeId;
	private String params;
	private long interval;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getResId() {
		return resId;
	}

	public void setResId(String resId) {
		this.resId = resId;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getExecuteId() {
		return executeId;
	}

	public void setExecuteId(String executeId) {
		this.executeId = executeId;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public String getMonitorName() {
		return monitorName;
	}

	public void setMonitorName(String monitorName) {
		this.monitorName = monitorName;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public AutoRecoverRecord(String id, String monitorId, String monitorName, String resId, String hostName, Date time,
			String tenantId, String executeId, String params, long interval) {
		this.id = id;
		this.monitorId = monitorId;
		this.monitorName = monitorName;
		this.resId = resId;
		this.hostName = hostName;
		this.time = time;
		this.tenantId = tenantId;
		this.executeId = executeId;
		this.params = params;
		this.interval = interval;
	}

	public AutoRecoverRecord() {
	}
}
