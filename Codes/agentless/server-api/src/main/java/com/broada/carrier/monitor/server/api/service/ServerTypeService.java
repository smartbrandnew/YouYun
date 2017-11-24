package com.broada.carrier.monitor.server.api.service;

import com.broada.carrier.monitor.server.api.entity.MonitorType;


/**
 * 监测器类型服务
 * @author Jiangjw
 */
public interface ServerTypeService extends BaseTypeService {
	/**
	 * 获取可以用于指定监测项类型的监测器类型
	 * @param targetTypeId
	 * @return
	 */
	MonitorType[] getTypesByTargetTypeId(String targetTypeId);	
}
