package uyun.bat.monitor.api.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Monitor implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;
	/**
	 * 创建用户id
	 */
	private String creatorId;
	/**
	 * 通知内容
	 */
	private String message;
	/**
	 * 监测器名称
	 */
	private String name;
	/**
	 * 查询逻辑表达式<br>
	 * 事件：最近某段时间，某类事件发生的次数大于阈值，则发生告警<br>
	 * events('sources:datadog priority:low status:warning
	 * "开门"').by('resourceId').rollup('count').last('5m') > 2<br>
	 * 指标：最近某段时间，某指标的汇聚值大于阈值，则发生告警<br>
	 * avg(last_5m):avg:system.cpu.system{!host:jianglf-centos6.7-64,role:1} by
	 * {resourceId} > 4
	 */
	private String query;
	/**
	 * 监测器类型
	 */
	private MonitorType monitorType;
	/**
	 * 监测器状态
	 */
	private MonitorState monitorState;
	/**
	 * 租户id
	 */
	private String tenantId;
	/**
	 * 是否发送通知
	 */
	private Boolean notify;
	/**
	 * 是否启用,为了过滤查询用Boolean
	 */
	private Boolean enable;
	/**
	 * 最后修改时间
	 */
	private Date modified;
	/**
	 * 通知用户id列表
	 */
	private List<String> notifyUserIdList;

	/**
	 * 监测器 告警策略的可选设置 由于事件监测器和指标监测器的参数不一样，<br>
	 * Options需要进行的设置也不一样
	 */
	private Options options;

	private Date createTime;
	/**
	 * automation自愈字段
	 */
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

	public MonitorType getMonitorType() {
		return monitorType;
	}

	public void setMonitorType(MonitorType monitorType) {
		this.monitorType = monitorType;
	}

	public MonitorState getMonitorState() {
		return monitorState;
	}

	public void setMonitorState(MonitorState monitorState) {
		this.monitorState = monitorState;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public Boolean getNotify() {
		return notify;
	}

	public void setNotify(Boolean notify) {
		this.notify = notify;
	}

	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
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

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public AutoRecoveryParams getAutoRecoveryParams() {
		return autoRecoveryParams;
	}

	public void setAutoRecoveryParams(AutoRecoveryParams autoRecoveryParams) {
		this.autoRecoveryParams = autoRecoveryParams;
	}
}
