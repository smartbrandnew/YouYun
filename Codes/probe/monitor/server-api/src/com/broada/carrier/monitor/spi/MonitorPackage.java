package com.broada.carrier.monitor.spi;

import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;

/**
 * 监测器类型扩展包
 * @author Jiangjw
 */
public interface MonitorPackage {
	/**
	 * 获取扩展的监测器类型定义
	 * @return
	 */
	MonitorType[] getTypes();
	
	/**
	 * 获取扩展的监测指标定义
	 * @return
	 */
	MonitorItem[] getItems();
	
	/**
	 * 获取扩展的监测方法类型定义
	 * @return
	 */
	MonitorMethodType[] getMethodTypes();
}
