package com.broada.carrier.monitor.impl.common.entity;

import com.broada.carrier.monitor.server.api.entity.MonitorState;

public class MonitorConstant {

	public static final MonitorState MONITORSTATE_NICER = MonitorState.SUCCESSED;
	public static final MonitorState MONITORSTATE_OVERSTEP = MonitorState.OVERSTEP;
	public static final MonitorState MONITORSTATE_FAILING = MonitorState.FAILED;
	public static final MonitorState MONITORSTATE_CANCEL = MonitorState.FAILED;

	public static final Double UNKNOWN_DOUBLE_VALUE = null;
	public static final String UNKNOWN_STRING_VALUE = null;

	public static final String UNKNOWN_VALUE_STRING_DESCR = "[未知]";
	public static final String FAILED_VALUE_STRING_DESCR = "获取失败";

}
