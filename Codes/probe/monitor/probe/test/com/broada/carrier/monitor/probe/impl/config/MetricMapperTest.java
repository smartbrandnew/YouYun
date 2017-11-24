package com.broada.carrier.monitor.probe.impl.config;


import org.junit.Test;

public class MetricMapperTest {

	@Test
	public void testGerRemotePerfName() {
		System.out.println("jboss perf name: "+MetricMapper.getInstance().getRemoteMetricType("IBMSVC-STOREPOOL-INFO","perf.usagespace_allocated"));
	}

	@Test
	public void testGetRemoteStateName() {
		System.out.println("weblogic remote name:"+MetricMapper.getInstance().getRemoteMetricType("IBMSVC-STOREPOOL-INFO","state.available_status"));
	}

}
