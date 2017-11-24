package com.broada.carrier.monitor.probe.impl.logic;

import com.broada.carrier.monitor.server.api.entity.MonitorTask;

/**
 * MonitorServiceFactory的监测任务变更事件监听器
 * @author Jiangjw
 */
public interface TaskListener {
	/**
	 * 增加了新的监测任务通知
	 * @param srv
	 */
	void onCreated(MonitorTask srv);

	/**
	 * 监测修改修改通知
	 * @param srv
	 */
	void onChanged(MonitorTask srv);

	/**
	 * 监测任务删除通知
	 * @param srv
	 */
	void onDelete(MonitorTask srv);
}
