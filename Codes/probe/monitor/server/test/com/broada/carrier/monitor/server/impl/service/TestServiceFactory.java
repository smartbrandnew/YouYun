package com.broada.carrier.monitor.server.impl.service;

import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
import com.broada.carrier.monitor.server.api.client.restful.RestfulServerServiceFactory;
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
import com.broada.carrier.monitor.server.impl.TestRuntime;

public class TestServiceFactory implements ServerServiceFactory {
	private static TestServiceFactory instance;
	private ServerServiceFactory proxy;		

	/**
	 * 获取默认实例
	 * @return
	 */
	public static TestServiceFactory getDefault() {
		if (instance == null) {
			synchronized (TestServiceFactory.class) {
				if (instance == null)
					instance = new TestServiceFactory();
			}
		}
		return instance;
	}
	
	public TestServiceFactory() {
		if (System.getProperty("monitor.test.server", "false").equals("true"))
			proxy = new RestfulServerServiceFactory("localhost", 9140);
	}

	@Override
	public ServerTypeService getTypeService() {		
		if (proxy != null)
			return proxy.getTypeService();
		else
			return TestRuntime.checkBean(ServerTypeService.class);
	}

	@Override
	public ServerTargetTypeService getTargetTypeService() {
		if (proxy != null)
			return proxy.getTargetTypeService();
		else
			return TestRuntime.checkBean(ServerTargetTypeService.class);
	}

	@Override
	public ServerTargetGroupService getTargetGroupService() {
		if (proxy != null)
			return proxy.getTargetGroupService();
		else
			return TestRuntime.checkBean(ServerTargetGroupService.class);
	}

	@Override
	public ServerProbeService getProbeService() {
		if (proxy != null)
			return proxy.getProbeService();
		else
			return TestRuntime.checkBean(ServerProbeService.class);
	}

	@Override
	public ServerNodeService getNodeService() {
		if (proxy != null)
			return proxy.getNodeService();
		else
			return TestRuntime.checkBean(ServerNodeService.class);
	}

	@Override
	public ServerTaskService getTaskService() {
		if (proxy != null)
			return proxy.getTaskService();
		else
			return TestRuntime.checkBean(ServerTaskService.class);
	}

	@Override
	public ServerResourceService getResourceService() {
		if (proxy != null)
			return proxy.getResourceService();
		else
			return TestRuntime.checkBean(ServerResourceService.class);
	}

	@Override
	public ServerPolicyService getPolicyService() {
		if (proxy != null)
			return proxy.getPolicyService();
		else
			return TestRuntime.checkBean(ServerPolicyService.class);
	}

	@Override
	public ServerMethodService getMethodService() {
		if (proxy != null)
			return proxy.getMethodService();
		else
			return TestRuntime.checkBean(ServerMethodService.class);
	}

	@Override
	public ServerSystemService getSystemService() {
		if (proxy != null)
			return proxy.getSystemService();
		else
			return TestRuntime.checkBean(ServerSystemService.class);
	}

	@Override
	public ServerFileService getFileService() {
		if (proxy != null)
			return proxy.getFileService();
		else
			return TestRuntime.checkBean(ServerFileService.class);
	}

	@Override
	public void login(String username, String password) {
	}

	@Override
	public void logout() {
	}
}
