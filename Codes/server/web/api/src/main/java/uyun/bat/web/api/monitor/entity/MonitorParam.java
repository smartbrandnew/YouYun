package uyun.bat.web.api.monitor.entity;

import java.util.List;

import uyun.bat.monitor.api.entity.AutoRecoveryParams;
import uyun.bat.monitor.api.entity.Options;

public class MonitorParam {
	private String id;
	private String creatorId;
	private String message;
	private String name;
	private String query;
	private String monitorType;
	@Deprecated
	private String monitorStatus;
	private String tenantId;
	private boolean notify = true;
	private Boolean enable = true;
	private List<String> notifyUserIdList;
	private Options options;
	private AutoRecoveryParams autoRecoveryParams;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getMonitorType() {
		return monitorType;
	}

	public void setMonitorType(String monitorType) {
		this.monitorType = monitorType;
	}

	@Deprecated
	public String getMonitorStatus() {
		return monitorStatus;
	}

	@Deprecated
	public void setMonitorStatus(String monitorStatus) {
		this.monitorStatus = monitorStatus;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public boolean isNotify() {
		return notify;
	}

	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public List<String> getNotifyUserIdList() {
		return notifyUserIdList;
	}

	public void setNotifyUserIdList(List<String> notifyUserIdList) {
		this.notifyUserIdList = notifyUserIdList;
	}

	public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}

	public AutoRecoveryParams getAutoRecoveryParams() {
		return autoRecoveryParams;
	}

	public void setAutoRecoveryParams(AutoRecoveryParams autoRecoveryParams) {
		this.autoRecoveryParams = autoRecoveryParams;
	}
}
