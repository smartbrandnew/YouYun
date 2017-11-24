package uyun.bat.monitor.api.common.util;

import uyun.bat.monitor.api.entity.MonitorType;


/**
 * 与datastore -- checkpoint相关的一些常用参数及方法
 */
public abstract class StateUtil {
	public static final String MONITOR_ID = "monitorId";
	public static final String TENANT_ID = "tenantId";
	public static final String RESOURCE_ID="resourceId";
	public static final String HOST="host";

	/**
	 * Checkpoint state<br>
	 * 指标监测器--uyun.monitor.metric.state
	 */
	public static final String MONITOR_METRIC_STATE = "uyun.monitor.metric.state";
	/**
	 * Checkpoint state<br>
	 * 事件监测器--uyun.monitor.event.state
	 */
	public static final String MONITOR_EVENT_STATE = "uyun.monitor.event.state";

	/**
	 * Checkpoint state<br>
	 * 主机监测器--uyun.monitor.metric.state
	 */
	public static final String RESOURCE_ONLINE_STATE = "uyun.resource.onlineState";

	/**
	 * Checkpoint state<br>
	 * 应用监测器--uyun.monitor.metric.state
	 */
	public static final String MONITOR_APP_STATE = "uyun.monitor.app.state";
	
	/**
	 * 创建检查点的state
	 * @param monitorType
	 * @return
	 */
	public static String generateState(MonitorType monitorType) {
		if (MonitorType.METRIC.equals(monitorType))
			return MONITOR_METRIC_STATE;
		else if (MonitorType.EVENT.equals(monitorType))
			return MONITOR_EVENT_STATE;
		else if(MonitorType.HOST.equals(monitorType))
			return RESOURCE_ONLINE_STATE;
		else if (MonitorType.APP.equals(monitorType))
			return MONITOR_APP_STATE;
		return null;
	}
}
