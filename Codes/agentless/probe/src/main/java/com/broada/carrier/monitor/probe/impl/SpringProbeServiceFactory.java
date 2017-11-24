package com.broada.carrier.monitor.probe.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.api.client.ProbeServiceFactory;
import com.broada.carrier.monitor.probe.api.service.ProbeFileService;
import com.broada.carrier.monitor.probe.api.service.ProbeMethodService;
import com.broada.carrier.monitor.probe.api.service.ProbeNodeService;
import com.broada.carrier.monitor.probe.api.service.ProbePolicyService;
import com.broada.carrier.monitor.probe.api.service.ProbeResourceService;
import com.broada.carrier.monitor.probe.api.service.ProbeSystemService;
import com.broada.carrier.monitor.probe.api.service.ProbeTaskService;

public class SpringProbeServiceFactory implements ProbeServiceFactory {
	@Autowired
	private ProbeSystemService probeService;
	@Autowired
	private ProbeNodeService nodeService;
	@Autowired
	private ProbeResourceService resourceService;
	@Autowired
	private ProbeMethodService methodService;
	@Autowired
	private ProbePolicyService policyService;
	@Autowired
	private ProbeTaskService taskService;
	@Autowired
	private ProbeFileService fileService;

	@Override
	public ProbeFileService getFileService() {
		return fileService;
	}

	@Override
	public ProbeSystemService getSystemService() {
		return probeService;
	}

	@Override
	public ProbeNodeService getNodeService() {
		return nodeService;
	}

	@Override
	public ProbeTaskService getTaskService() {
		return taskService;
	}

	@Override
	public ProbeResourceService getResourceService() {
		return resourceService;
	}

	@Override
	public ProbePolicyService getPolicyService() {
		return policyService;
	}

	@Override
	public ProbeMethodService getMethodService() {
		return methodService;
	}

}
