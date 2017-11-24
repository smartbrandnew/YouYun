package com.broada.carrier.monitor.probe.impl.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;

import org.junit.Test;

import com.broada.carrier.monitor.probe.api.service.ProbeMethodService;
import com.broada.carrier.monitor.probe.impl.TestRuntime;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class TestMethodService {

	@Test
	public void test() {
		ProbeMethodService service = TestRuntime.getServiceFactory().getMethodService();
		MonitorMethod[] methods = service.getMethods();
		assertNotNull(methods);
		
		HashMap<String, Object> options = new HashMap<String, Object>();
		options.put("key1", "value1");
		options.put("key2", "value2");
		MonitorMethod method = new MonitorMethod("code", "name", "test", null, options, System.currentTimeMillis(), null);		
		service.saveMethod(method);
		
		methods = service.getMethods();
		MonitorMethod method2 = getMethod(methods, method.getCode());		
		assertEquals(method, method2);
		
		service.deleteMethod(method.getCode());
		methods = service.getMethods();
		method2 = getMethod(methods, method.getCode());		
		assertNull(method2);
	}

	private MonitorMethod getMethod(MonitorMethod[] methods, String code) {
		for (MonitorMethod method : methods) 
			if (method.getCode().equals(code))
				return method;
		return null;
	}

}
