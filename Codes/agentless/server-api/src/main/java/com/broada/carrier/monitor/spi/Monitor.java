package com.broada.carrier.monitor.spi;

import java.io.Serializable;

import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * 监测器具体实现
 * @author Jiangjw
 */
public interface Monitor {
	/**
	 * probe监测调度采集
	 * 当监测器调度周期触发时调用
	 * @param context 输入相关参数，包括node, resource, method等
	 * @return 返回监测结果
	 */
	MonitorResult monitor(MonitorContext context);

	/**
	 * client界面操作采集
	 * 当配置人员在进行监测配置界面操作时，如果操作需要采集目标设备的数据，则可调用到此方法
	 * @param context 输入相关参数，包括node, resource, method等
	 * @return 返回采集结果
	 */
	Serializable collect(CollectContext context);
	
}