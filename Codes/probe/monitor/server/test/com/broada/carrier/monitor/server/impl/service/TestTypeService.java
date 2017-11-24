package com.broada.carrier.monitor.server.impl.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.broada.carrier.monitor.server.api.service.ServerTypeService;
import com.broada.carrier.monitor.server.impl.TestRuntime;

public class TestTypeService {

	@Test
	public void test() {
		ServerTypeService service = TestRuntime.getServiceFactory().getTypeService();
		assertNotNull(service.getTypes());
		assertEquals(0, service.getTypesByTargetTypeId("hello").length);
	}
}
