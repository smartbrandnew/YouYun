package com.broada.carrier.monitor.probe.impl.service;

import org.junit.Test;

import com.broada.carrier.monitor.probe.api.service.ProbeSystemService;
import com.broada.carrier.monitor.probe.impl.TestRuntime;

public class TestProbeService {

	@Test
	public void test() {
		ProbeSystemService service = TestRuntime.getServiceFactory().getSystemService();
		service.deleteAll();
	}

}
