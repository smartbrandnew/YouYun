package com.broada.carrier.monitor.server.api.service;

import com.broada.carrier.monitor.server.api.entity.MonitorTargetGroup;

/**
 * 监测导航服务
 * @author Jiangjw
 */
public interface ServerTargetGroupService {
	/**
	 * 获取指定导航节点的子节点
	 * @param parentId 如果为null则表示查询根节点
	 * @return
	 */
	MonitorTargetGroup[] getGroupsByParentId(String parentId);
}
