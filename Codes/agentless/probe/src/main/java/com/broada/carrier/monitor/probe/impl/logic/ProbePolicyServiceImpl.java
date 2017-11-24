package com.broada.carrier.monitor.probe.impl.logic;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.impl.dispatch.MonitorDispatcher;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;

// TODO 2015-02-26 09:39:32 实现缓存化
public class ProbePolicyServiceImpl implements ProbePolicyServiceEx {
	private ProbePolicyServiceEx target;
	@Autowired
	private ProbeTaskServiceEx taskService;
	@Autowired
	private MonitorDispatcher dispatcher;
	
	public ProbePolicyServiceImpl(ProbePolicyServiceEx target) {
		this.target = target;
	}

	@Override
	public MonitorPolicy[] getPolicies() {
		return target.getPolicies();
	}

	@Override
	public void savePolicy(MonitorPolicy policy) {
		target.savePolicy(policy);
		MonitorTask[] tasks = taskService.getTasksByPolicyCode(policy.getCode());
		for (MonitorTask task : tasks) 
			dispatcher.rescheduleTask(task);
	}

	@Override
	public OperatorResult deletePolicy(String policyCode) {
		return target.deletePolicy(policyCode);
	}

	@Override
	public MonitorPolicy getPolicy(String policyCode) {
		return target.getPolicy(policyCode);
	}

	public MonitorPolicy checkPolicy(String policyCode) {
		MonitorPolicy policy = getPolicy(policyCode);
		if (policy == null)
			throw new IllegalArgumentException("不存在的监测策略：" + policyCode);
		return policy;
	}

	@Override
	public void deleteAll() {
		target.deleteAll();
	}
}
