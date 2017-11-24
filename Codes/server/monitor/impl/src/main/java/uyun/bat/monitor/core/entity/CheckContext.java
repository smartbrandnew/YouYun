package uyun.bat.monitor.core.entity;

import uyun.bat.event.api.entity.Event;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;

public class CheckContext {
	/** monitor检测器类型 */
	private MonitorType monitorType;

	/** 已触发的事件 */
	private Event event;

	/** 事件监测器的ip可能哪不到，故要去datastore查一遍 */
	private String resId;

	/** 指标监测器，下部分数据应该能从tag中拿到 */
	private String hostName;
	private String ip;
	/** 指标监测器，根据分组产生的细化对象实例 如 device tablespaceName */
	private String instance;

	/** 指标的当前值 */
	private String value;

	/** 当前监测器的告警状态 */
	private MonitorState monitorState;

	/**
	 * 上次检查点的状态(本次触发前的状态)
	 */
	private MonitorState lastMonitorState;

	/**
	 * 监测器参数
	 */
	private MonitorParam monitorParam;

	/**
	 * 关联的事件总数
	 */
	private int count;

	/**
	 * 事件恢复条件
	 */
	private String[] eventRecover;

	/**
	 * 发送到abt的hostId
	 * 格式hostId+app
	 */
	private String hostId;

	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public MonitorType getMonitorType() {
		return monitorType;
	}
	
	public void setMonitorType(MonitorType monitorType) {
		this.monitorType = monitorType;
	}
	
	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public String getResId() {
		return resId;
	}

	public void setResId(String resId) {
		this.resId = resId;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public MonitorState getMonitorState() {
		return monitorState;
	}

	public void setMonitorState(MonitorState monitorState) {
		this.monitorState = monitorState;
	}

	public MonitorParam getMonitorParam() {
		return monitorParam;
	}

	public MonitorState getLastMonitorState() {
		return lastMonitorState;
	}

	public void setLastMonitorState(MonitorState lastMonitorState) {
		this.lastMonitorState = lastMonitorState;
	}

	public void setMonitorParam(MonitorParam monitorParam) {
		this.monitorParam = monitorParam;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String[] getEventRecover() {
		return eventRecover;
	}

	public void setEventRecover(String[] eventRecover) {
		this.eventRecover = eventRecover;
	}

	public CheckContext() {
		super();
	}

	public CheckContext(Event event, String resId, MonitorState monitorState, MonitorParam monitorParam) {
		super();
		this.event = event;
		this.resId = resId;
		this.monitorState = monitorState;
		this.monitorParam = monitorParam;
	}

	public CheckContext(Event event, String resId, MonitorState monitorState, String value, MonitorParam monitorParam,MonitorType monitorType) {
		super();
		this.event = event;
		this.resId = resId;
		this.monitorState = monitorState;
		this.value = value;
		this.monitorParam = monitorParam;
		this.monitorType = monitorType;
	}

	public CheckContext(Event event, String resId, String hostName, String ip, String value, MonitorState monitorState,
			MonitorParam monitorParam,MonitorType monitorType) {
		super();
		this.event = event;
		this.resId = resId;
		this.hostName = hostName;
		this.ip = ip;
		this.value = value;
		this.monitorState = monitorState;
		this.monitorParam = monitorParam;
		this.monitorType = monitorType;
	}

}
