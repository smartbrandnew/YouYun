package com.broada.carrier.monitor.probe.impl.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.broada.carrier.monitor.probe.api.service.ProbePolicyService;
import com.broada.carrier.monitor.probe.impl.TestRuntime;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;

public class TestPolicyService {

	@Test
	public void test() {
		ProbePolicyService service = TestRuntime.getServiceFactory().getPolicyService();
		MonitorPolicy[] policies = service.getPolicies();
		assertNotNull(policies);
		
		MonitorPolicy policy = new MonitorPolicy("code", "name", 600, 600);
		policy.setModified(System.currentTimeMillis());
		service.savePolicy(policy);
		
		policies = service.getPolicies();
		MonitorPolicy policy2 = getPolicy(policies, policy.getCode());		
		assertEquals(policy, policy2);
		
		service.deletePolicy(policy.getCode());
		policies = service.getPolicies();
		policy2 = getPolicy(policies, policy.getCode());		
		assertNull(policy2);
	}

	private MonitorPolicy getPolicy(MonitorPolicy[] policies, String code) {
		for (MonitorPolicy policy : policies) 
			if (policy.getCode().equals(code))
				return policy;
		return null;
	}

}
