package com.broada.carrier.monitor.server.api.service;

import java.util.Map;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetStatus;

/**
 * 监测资源管理服务
 * 本服务的API均受权限控制
 * @author Jiangjw
 */
public interface ServerResourceService extends BaseResourceService {
	/**
	 * 查询指定组的监测资源数据
	 * @param pageNo
	 * @param groupId
	 * @return
	 */
	Page<MonitorResource> getResourcesByGroupId(PageNo pageNo, String groupId);
	
	/**
	 * 查询指定探针的监测资源数据
	 * @param pageNo
	 * @param probeId
	 * @return
	 */
	MonitorResource[] getResourcesByNodeId(String nodeId);
	
	/**
	 * 查询资源所属节点
	 * @param resourceId
	 * @return
	 */
	String getResourceNodeId(String resourceId);
	
	Map<String, String> getResourceNodeId(String [] resourceIds);
	
	MonitorTargetStatus getResourceStatus(String resourceId);
	
	MonitorTargetStatus[] getResourcesStatus(String [] resourceIds);
	
	/**
	 * 批量查询节点下关联的资源数据
	 * @param ids 批量节点id，按","分割
	 * @return
	 */
	Page<MonitorResource> getResourcesByNodeIds(String ids);
}
