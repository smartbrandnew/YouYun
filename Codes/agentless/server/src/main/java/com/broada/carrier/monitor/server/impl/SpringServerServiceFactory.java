package com.broada.carrier.monitor.server.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
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

public class SpringServerServiceFactory implements ServerServiceFactory {
	@Autowired
	private ServerProbeService probeService;
	@Autowired
	private ServerNodeService nodeService;
	@Autowired
	private ServerResourceService resourceService;
	@Autowired
	private ServerMethodService methodService;
	@Autowired
	private ServerPolicyService policyService;
	@Autowired
	private ServerTaskService taskService;
	@Autowired
	private ServerTypeService typeService;
	@Autowired
	private ServerTargetTypeService targetTypeService;
	@Autowired
	private ServerTargetGroupService targetGroupService;
	@Autowired
	private ServerSystemService systemService;
	@Autowired
	private ServerFileService fileService;	
	
	private static SpringServerServiceFactory instance;

	/**
	 * 获取默认实例
	 * @return
	 */
	public static SpringServerServiceFactory getDefault() {
		if (instance == null) {
			synchronized (SpringServerServiceFactory.class) {
				if (instance == null)
					instance = new SpringServerServiceFactory();
			}
		}
		return instance;
	}

	@Override
	public ServerProbeService getProbeService() {
		return probeService;
	}

	@Override
	public ServerNodeService getNodeService() {
		return nodeService;
	}

	@Override
	public ServerTaskService getTaskService() {
		return taskService;
	}

	@Override
	public ServerResourceService getResourceService() {
		return resourceService;
	}

	@Override
	public ServerPolicyService getPolicyService() {
		return policyService;
	}

	@Override
	public ServerMethodService getMethodService() {
		return methodService;
	}

	@Override
	public ServerTypeService getTypeService() {
		return typeService;
	}

	@Override
	public ServerTargetTypeService getTargetTypeService() {
		return targetTypeService;
	}

	@Override
	public ServerTargetGroupService getTargetGroupService() {
		return targetGroupService;
	}

	@Override
	public ServerSystemService getSystemService() {
		return systemService;
	}

	@Override
	public ServerFileService getFileService() {
		return fileService;
	}

	@Override
	public void login(String username, String password) {	
	}

	@Override
	public void logout() {
	}

}
