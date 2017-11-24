package com.broada.carrier.monitor.server.impl.service;

import org.junit.Assert;
import org.junit.Test;

import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.carrier.monitor.server.api.service.ServerTargetTypeService;
import com.broada.carrier.monitor.server.impl.TestRuntime;

public class TestTargetTypeServiceImpl {

	@Test
	public void test() {
		ServerTargetTypeService service = TestRuntime.getServiceFactory().getTargetTypeService();		
		MonitorTargetType[] nodes = service.getTargetTypesByNode();
		assertExists(nodes, "Computer");		
		
		MonitorTargetType[] resources = service.getTargetTypesByResource();
		assertExists(resources, "DB");		
	}

	private void assertExists(MonitorTargetType[] types, String typeId) {
		for (MonitorTargetType type : types)
			if (type.getId().equals(typeId))
				return;
		Assert.fail("not exists type: " + typeId);		
	}

}
