package com.broada.carrier.monitor.server.api.client.restful;

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

public class RestfulServerServiceFactory implements ServerServiceFactory {
	private ServerTypeClient typeClient;
	private ServerProbeClient probeClient;
	private ServerTaskClient taskClient;
	private ServerNodeClient nodeClient;
	private ServerResourceClient resourceClient;
	private ServerPolicyClient policyClient;
	private ServerMethodClient methodClient;
	private ServerTargetTypeClient targetTypeClient;
	private ServerTargetGroupClient targetGroupClient;
	private ServerSystemClient systemClient;
	private ServerFileClient fileClient;
	private String baseServiceUrl;
	private String sessionId;
	
	public RestfulServerServiceFactory(String baseServiceUrl) {
		this.baseServiceUrl = baseServiceUrl;
	}
	
	public RestfulServerServiceFactory(String protocol, String host, int port) {
		this(protocol + "://" + host + ":" + port);
	}
	
	public RestfulServerServiceFactory(String host, int port) {
		this("http", host, port);
	}
	
	public String getBaseServiceUrl() {
		return baseServiceUrl;
	}

	@Override
	public ServerTypeService getTypeService() {
		if (typeClient == null)
			typeClient = new ServerTypeClient(baseServiceUrl);
		return typeClient;
	}

	@Override
	public ServerTargetTypeService getTargetTypeService() {
		if (targetTypeClient == null)
			targetTypeClient = new ServerTargetTypeClient(baseServiceUrl);
		return targetTypeClient;
	}

	@Override
	public ServerTargetGroupService getTargetGroupService() {
		if (targetGroupClient == null)
			targetGroupClient = new ServerTargetGroupClient(baseServiceUrl);
		return targetGroupClient;
	}

	@Override
	public ServerProbeService getProbeService() {
		if (probeClient == null)
			probeClient = new ServerProbeClient(baseServiceUrl);
		return probeClient;
	}

	@Override
	public ServerNodeService getNodeService() {
		if (nodeClient == null)
			nodeClient = new ServerNodeClient(baseServiceUrl);
		return nodeClient;
	}

	@Override
	public ServerTaskService getTaskService() {
		if (taskClient == null)
			taskClient = new ServerTaskClient(baseServiceUrl);
		return taskClient;
	}

	@Override
	public ServerResourceService getResourceService() {
		if (resourceClient == null)
			resourceClient = new ServerResourceClient(baseServiceUrl);
		return resourceClient;
	}

	@Override
	public ServerPolicyService getPolicyService() {
		if (policyClient == null)
			policyClient = new ServerPolicyClient(baseServiceUrl);
		return policyClient;
	}	
	
	@Override
	public ServerMethodService getMethodService() {
		if (methodClient == null)
			methodClient = new ServerMethodClient(baseServiceUrl);
		return methodClient;
	}

	@Override
	public ServerSystemService getSystemService() {
		if (systemClient == null)
			systemClient = new ServerSystemClient(baseServiceUrl);
		return systemClient;
	}

	@Override
	public ServerFileService getFileService() {
		if (fileClient == null)
			fileClient = new ServerFileClient(baseServiceUrl);
		return fileClient;
	}

	@Override
	public void login(String username, String password) {		
		sessionId = getSystemService().login(username, password);
		String code = "sessionId";
		((ServerTypeClient)getTypeService()).getClient().setRequestHeader(code, sessionId);
		((ServerProbeClient)getProbeService()).getClient().setRequestHeader(code, sessionId);
		((ServerTaskClient)getTaskService()).getClient().setRequestHeader(code, sessionId);
		((ServerNodeClient)getNodeService()).getClient().setRequestHeader(code, sessionId);
		((ServerResourceClient)getResourceService()).getClient().setRequestHeader(code, sessionId);
		((ServerPolicyClient)getPolicyService()).getClient().setRequestHeader(code, sessionId);
		((ServerMethodClient)getMethodService()).getClient().setRequestHeader(code, sessionId);
		((ServerTargetTypeClient)getTargetTypeService()).getClient().setRequestHeader(code, sessionId);
		((ServerTargetGroupClient)getTargetGroupService()).getClient().setRequestHeader(code, sessionId);
		((ServerSystemClient)getSystemService()).getClient().setRequestHeader(code, sessionId);
		((ServerFileClient)getFileService()).getClient().setRequestHeader(code, sessionId);
	}

	@Override
	public void logout() {
		getSystemService().logout(sessionId);
	}	
}
