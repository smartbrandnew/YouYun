package com.broada.carrier.monitor.server.api.service;


import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;

/**
 * 监测器类型服务
 * 
 * @author Jiangjw
 */
public interface BaseTypeService {
	/**
	 * 获取所有监测器类型
	 * 
	 * @return
	 */
	MonitorType[] getTypes();

	MonitorType getType(String typeId);

	/**
	 * 获取所有预定义的监测指标
	 * 
	 * @return
	 */
	MonitorItem[] getItems();

	MonitorItem getItem(String itemCode);

	MonitorMethodType getMethodType(String typeId);
}
