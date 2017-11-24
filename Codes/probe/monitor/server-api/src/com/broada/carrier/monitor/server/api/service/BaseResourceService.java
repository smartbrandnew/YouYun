package com.broada.carrier.monitor.server.api.service;

import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;

/**
 * 监测资源管理服务
 * 本服务的API均受权限控制
 * @author Jiangjw
 */
public interface BaseResourceService {
	MonitorResource getResource(String resourceId);

	/**
	 * 保存监测资源，如果resource.id为空，则添加，如果resource.id不为空，则修改
	 * @param resource
	 * @return 如果保存成功返回resource.id
	 */
	String saveResource(MonitorResource resource);

	/**
	 * 删除监测资源
	 * @param id
	 * @return
	 */
	OperatorResult deleteResource(String id);
}
