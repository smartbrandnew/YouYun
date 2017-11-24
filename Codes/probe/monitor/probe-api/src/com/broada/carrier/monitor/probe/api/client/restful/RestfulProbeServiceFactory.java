package com.broada.carrier.monitor.probe.api.client.restful;

import com.broada.carrier.monitor.probe.api.client.ProbeServiceFactory;
import com.broada.carrier.monitor.probe.api.service.ProbeFileService;
import com.broada.carrier.monitor.probe.api.service.ProbeMethodService;
import com.broada.carrier.monitor.probe.api.service.ProbeNodeService;
import com.broada.carrier.monitor.probe.api.service.ProbePolicyService;
import com.broada.carrier.monitor.probe.api.service.ProbeResourceService;
import com.broada.carrier.monitor.probe.api.service.ProbeSystemService;
import com.broada.carrier.monitor.probe.api.service.ProbeTaskService;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;

public class RestfulProbeServiceFactory implements ProbeServiceFactory {
	private ProbeSystemClient probeClient;
	private ProbeTaskClient taskClient;
	private ProbeNodeClient nodeClient;
	private ProbeResourceClient resourceClient;
	private ProbePolicyClient policyClient;
	private ProbeMethodClient methodClient;
	private ProbeFileClient fileClient;
	private String baseServiceUrl;

	public RestfulProbeServiceFactory(String baseServiceUrl) {
		this.baseServiceUrl = baseServiceUrl;
	}

	public RestfulProbeServiceFactory(String protocol, String host, int port) {
		this(protocol + "://" + host + ":" + port);
	}

	public RestfulProbeServiceFactory(String host, int port) {
		this("http", host, port);
	}

	public RestfulProbeServiceFactory(MonitorProbe probe) {
		this(probe.getHost(), probe.getPort());
	}

	@Override
	public ProbeSystemService getSystemService() {
		if (probeClient == null)
			probeClient = new ProbeSystemClient(baseServiceUrl);
		return probeClient;
	}

	@Override
	public ProbeNodeService getNodeService() {
		if (nodeClient == null)
			nodeClient = new ProbeNodeClient(baseServiceUrl);
		return nodeClient;
	}

	@Override
	public ProbeTaskService getTaskService() {
		if (taskClient == null)
			taskClient = new ProbeTaskClient(baseServiceUrl);
		return taskClient;
	}

	@Override
	public ProbeResourceService getResourceService() {
		if (resourceClient == null)
			resourceClient = new ProbeResourceClient(baseServiceUrl);
		return resourceClient;
	}

	@Override
	public ProbePolicyService getPolicyService() {
		if (policyClient == null)
			policyClient = new ProbePolicyClient(baseServiceUrl);
		return policyClient;
	}

	@Override
	public ProbeMethodService getMethodService() {
		if (methodClient == null)
			methodClient = new ProbeMethodClient(baseServiceUrl);
		return methodClient;
	}

	@Override
	public ProbeFileService getFileService() {
		if (fileClient == null)
			fileClient = new ProbeFileClient(baseServiceUrl);
		return fileClient;
	}
}
