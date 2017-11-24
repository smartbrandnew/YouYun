package com.broada.carrier.monitor.server.api.service;

import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;

/**
 * 监测节点管理服务
 * 本服务的API均受权限控制
 * @author Jiangjw
 */
public interface BaseNodeService {
	MonitorNode getNode(String nodeId);

	/**
	 * 保存监测节点，如果node.id为空，则添加，如果node.id不为空，则修改
	 * @param node
	 * @return 如果保存成功返回node.id
	 */
	String saveNode(MonitorNode node);
	
	/**
	 * 删除监测节点
	 * @param id
	 * @return
	 */
	OperatorResult deleteNode(String id);
}
