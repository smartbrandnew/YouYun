package com.broada.carrier.monitor.spi;

import java.io.Serializable;

import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.spi.entity.CollectContextV2;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

public interface MonitorV2 {
	/**
	 * probe监测调度采集
	 * 当监测器调度周期触发时调用
	 * @param context 输入相关参数，包括node, resource, method等
	 * @return 返回监测结果
	 */
	MonitorResult monitor(MonitorContext context);

	/**
	 * client界面操作采集
	 * 耗时任务的采集，请调用到此方法
	 * @param context 输入相关参数，包括node, resource, method等
	 * @return 返回采集结果
	 */
	Serializable collect(CollectContextV2 context);
}
