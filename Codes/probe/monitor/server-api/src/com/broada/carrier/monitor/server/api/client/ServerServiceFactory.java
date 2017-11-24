package com.broada.carrier.monitor.server.api.client;

import com.broada.carrier.monitor.server.api.service.ServerFileService;
import com.broada.carrier.monitor.server.api.service.ServerMethodService;
import com.broada.carrier.monitor.server.api.service.ServerNodeService;
import com.broada.carrier.monitor.server.api.service.ServerPolicyService;
import com.broada.carrier.monitor.server.api.service.ServerProbeService;
import com.broada.carrier.monitor.server.api.service.ServerResourceService;
import com.broada.carrier.monitor.server.api.service.ServerSystemService;
import com.broada.carrier.monitor.server.api.service.ServerTargetGroupService;
import com.broada.carrier.monitor.server.api.service.ServerTargetTypeService;
import com.broada.carrier.monitor.server.api.service.ServerTaskService;
import com.broada.carrier.monitor.server.api.service.ServerTypeService;

/**
 * 服务工厂
 * @author Jiangjw
 */
public interface ServerServiceFactory {
	/**
	 * 获取监测器类型服务
	 * @return
	 */
	ServerTypeService getTypeService();
	
	/**
	 * 获取监测项类型服务
	 * @return
	 */
	ServerTargetTypeService getTargetTypeService();
	 
	/**
	 * 获取监测项分组服务
	 * @return
	 */
	ServerTargetGroupService getTargetGroupService();
	
	/**
	 * 获取监测项探针管理服务
	 * @return
	 */
	ServerProbeService getProbeService();
	
	/**
	 * 获取监测项节点管理服务
	 * @return
	 */
	ServerNodeService getNodeService();
	
	/**
	 * 获取监测任务管理服务
	 * @return
	 */
	ServerTaskService getTaskService();

	ServerResourceService getResourceService();

	ServerPolicyService getPolicyService();

	ServerMethodService getMethodService();
	
	ServerSystemService getSystemService();
	
	ServerFileService getFileService();
	
	void login(String username, String password);
	
	void logout();
}
