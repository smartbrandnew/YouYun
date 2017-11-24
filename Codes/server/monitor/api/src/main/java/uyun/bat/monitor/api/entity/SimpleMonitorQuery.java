package uyun.bat.monitor.api.entity;

import java.util.List;

public class SimpleMonitorQuery {
	/**
	 * 租户id
	 */
	private String tenantId;
	/**
	 * 监测器名称
	 */
	private String name;
	/**
	 * 监测器状态
	 */
	private List<MonitorState> monitorState;
	/**
	 * 是否启用,为了过滤查询用Boolean
	 */
	private Boolean enable;

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<MonitorState> getMonitorState() {
		return monitorState;
	}

	public void setMonitorState(List<MonitorState> monitorState) {
		this.monitorState = monitorState;
	}

	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public SimpleMonitorQuery() {
		super();
	}

	public SimpleMonitorQuery(String tenantId, String name, List<MonitorState> monitorState, Boolean enable) {
		super();
		this.tenantId = tenantId;
		this.name = name;
		this.monitorState = monitorState;
		this.enable = enable;
	}

}
