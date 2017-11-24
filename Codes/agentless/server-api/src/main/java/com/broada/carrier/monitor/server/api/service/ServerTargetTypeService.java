package com.broada.carrier.monitor.server.api.service;

import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;

/**
 * 监测项类型接口
 * @author Jiangjw
 */
public interface ServerTargetTypeService {
	/**
	 * 获取监测节点类型的所有根类型
	 * @return
	 */
	MonitorTargetType[] getTargetTypesByNode();
	
	/**
	 * 获取监测资源类型的所有根类型
	 * @return
	 */
	MonitorTargetType[] getTargetTypesByResource();
	
	/**
	 * 获取指定类型下的子类型
	 * @param parentId 不可以为null
	 * @return
	 */
	MonitorTargetType[] getTargetTypesByParentId(String parentId);

	MonitorTargetType getTargetType(String typeId);
}
