package com.broada.carrier.monitor.probe.api.client;

import com.broada.carrier.monitor.probe.api.service.ProbeFileService;
import com.broada.carrier.monitor.probe.api.service.ProbeNodeService;
import com.broada.carrier.monitor.probe.api.service.ProbePolicyService;
import com.broada.carrier.monitor.probe.api.service.ProbeMethodService;
import com.broada.carrier.monitor.probe.api.service.ProbeSystemService;
import com.broada.carrier.monitor.probe.api.service.ProbeResourceService;
import com.broada.carrier.monitor.probe.api.service.ProbeTaskService;

/**
 * 服务工厂
 * @author Jiangjw
 */
public interface ProbeServiceFactory {
	/**
	 * 获取监测项探针管理服务
	 * @return
	 */
	ProbeSystemService getSystemService();
	
	/**
	 * 获取监测项节点管理服务
	 * @return
	 */
	ProbeNodeService getNodeService();
	
	/**
	 * 获取监测任务管理服务
	 * @return
	 */
	ProbeTaskService getTaskService();

	ProbeResourceService getResourceService();

	ProbePolicyService getPolicyService();

	ProbeMethodService getMethodService();
	
	ProbeFileService getFileService();
}
