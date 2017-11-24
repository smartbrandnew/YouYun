package com.broada.carrier.monitor.server.impl.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.broada.carrier.monitor.common.error.ServiceException;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.service.ServerProbeService;
import com.broada.carrier.monitor.server.impl.TestRuntime;

public class TestProbeService {

	@Test
	public void test() {
		ServerProbeService service = TestRuntime.getServiceFactory().getProbeService();
		assertNotNull(service.getProbes());
		
		MonitorProbe probe = service.getProbeByCode("test");
		if (probe != null)
			service.deleteProbe(probe.getId());
		
		probe = new MonitorProbe();
		probe.setCode("test");
		probe.setName("测试");
		probe.setHost("localhost");
		probe.setPort(8890);
		probe.setId(service.saveProbe(probe));
		
		assertEquals(probe, service.getProbeByCode("test"));
		
		probe.setCode("test1");
		try {
			service.saveProbe(probe);
			fail();
		} catch (ServiceException e) {			
		}
		
		probe.setCode("test");		
		probe.setPort(8891);
		service.saveProbe(probe);
		assertEquals(probe, service.getProbeByCode("test"));
		assertEquals(probe, service.getProbeByHostPort("localhost", 8891));
		
		service.deleteProbe(probe.getId());
		assertNull(service.getProbeByCode(probe.getCode()));
	}
}
