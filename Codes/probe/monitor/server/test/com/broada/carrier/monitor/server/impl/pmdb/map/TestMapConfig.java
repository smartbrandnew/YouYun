package com.broada.carrier.monitor.server.impl.pmdb.map;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestMapConfig {

	@Test
	public void test() {
		MapConfig config = new MapConfig();
		MapMonitor monitor = config.getMonitor("ICMP");
		assertTrue(monitor.isEnabled());
		
		monitor = config.getMonitor("SNMP-HOSTINFO");
		assertTrue(monitor.isEnabled());
	}

}
