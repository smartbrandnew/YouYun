package com.broada.carrier.monitor.server.impl.entity;

import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ServerSideMonitorPolicy extends MonitorPolicy {
	private static final long serialVersionUID = 1L;
	public static final String CLASS_CODE = "MonitorPolicy";
	public static final String ATTR_INTERVAL = "monitorInterval";
	public static final String ATTR_ERROR_INTERVAL = "errorMonitorInterval";
	public static final String ATTR_WORK_WEEK_DAYS = "workWeekDays";
	public static final String ATTR_WORK_TIME_RANGE = "workTimeRange";	
	public static final String ATTR_STOP_TIME_RANGES = "stopTimeRanges";	
	private String id;

	public ServerSideMonitorPolicy() {
		super();
	}

	public ServerSideMonitorPolicy(String id, String code, String name, int interval, int errorInterval, String workWeekDays,
			String workTimeRange, String stopTimeRanges, String descr, long modified) {
		super(code, name, interval, errorInterval, workWeekDays, workTimeRange, stopTimeRanges, descr, modified);
		setId(id);
	}

	public ServerSideMonitorPolicy(String id, String code, String name, int interval, int errorInterval) {
		super(code, name, interval, errorInterval);
		setId(id);
	}

	public ServerSideMonitorPolicy(MonitorPolicy policy) {
		super(policy);
		if (policy instanceof ServerSideMonitorPolicy)
			setId(((ServerSideMonitorPolicy) policy).getId());
	}

	@JsonIgnore
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
