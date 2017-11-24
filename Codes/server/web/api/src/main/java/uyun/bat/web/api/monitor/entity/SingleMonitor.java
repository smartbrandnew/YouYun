package uyun.bat.web.api.monitor.entity;

import java.util.List;

import uyun.bat.monitor.api.entity.AutoRecoveryParams;
import uyun.bat.monitor.api.entity.Options;

/**
 * 获取单个monitor展现格式
 * 
 */
public class SingleMonitor {
	private String id;
	private String message;
	private String name;
	private String query;
	private String monitorType;
	private String monitorStatus;
	private boolean enable;
	private List<String> notifyUserIdList;
	private Options options;
	private AutoRecoveryParams autoRecoveryParams;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getMonitorStatus() {
		return monitorStatus;
	}

	public void setMonitorStatus(String monitorStatus) {
		this.monitorStatus = monitorStatus;
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

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public AutoRecoveryParams getAutoRecoveryParams() {
		return autoRecoveryParams;
	}

	public void setAutoRecoveryParams(AutoRecoveryParams autoRecoveryParams) {
		this.autoRecoveryParams = autoRecoveryParams;
	}
}
