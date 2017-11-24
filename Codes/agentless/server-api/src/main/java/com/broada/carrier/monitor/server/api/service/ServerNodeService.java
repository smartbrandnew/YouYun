package com.broada.carrier.monitor.server.api.service;

import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetStatus;

/**
 * 监测节点管理服务
 * 本服务的API均受权限控制
 * @author Jiangjw
 */
public interface ServerNodeService extends BaseNodeService {
	/**
	 * 查询指定组的监测节点数据
	 * @param pageNo
	 * @param groupId
	 * @return
	 */
	Page<MonitorNode> getNodesByGroupId(PageNo pageNo, String groupId);

	/**
	 * 查询指定探针的监测节点数据
	 * @param pageNo
	 * @param probeId
	 * @return
	 */
	Page<MonitorNode> getNodesByProbeId(PageNo pageNo, int probeId, boolean currentDomain);
	
	int getNodeProbeId(String nodeId);
	
	Map<String, Integer> getNodeProbeId(String[] nodeIds);
	
	/**
	 * 使用IP字段进行模糊查询
	 * @param pageNo
	 * @param ip 注意，这里的IP是模糊查询
	 * @return
	 */
	Page<MonitorNode> getNodesByIp(PageNo pageNo, String ip);
	
	MonitorTargetStatus getNodeStatus(String nodeId);
	
	MonitorTargetStatus[] getNodesStatus(String[] nodeIds);
	
	/**
	 * 取得所有的节点
	 * @param currentDomain 是否按域
	 * @return
	 */
	Page<MonitorNode> getNodes(boolean currentDomain);
	
	/**
	 * 根据id获取节点
	 * @param ids 节点id集合
	 * @param currentDomain 是否按域
	 * @return
	 */
	MonitorNode[] getNodes(List<String> ids, boolean currentDomain);
}
