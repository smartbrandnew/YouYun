package com.broada.carrier.monitor.server.impl.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.broada.carrier.monitor.server.api.entity.MonitorTargetGroup;
import com.broada.carrier.monitor.server.api.service.ServerTargetGroupService;
import com.broada.carrier.monitor.server.impl.TestRuntime;

public class TestTargetGroupServiceImpl {

	@Test
	public void test() {
		ServerTargetGroupService service = TestRuntime.getServiceFactory().getTargetGroupService();
		MonitorTargetGroup[] groups = service.getGroupsByParentId(null);
		assertNotNull(groups);
		assertTrue(groups.length > 0);
	}

}
