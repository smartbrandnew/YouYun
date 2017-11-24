package com.broada.carrier.monitor.impl.stdsvc.dns;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class TestDNSParameter {
	@Test
	public void test() {
		DNSMonitorCondition cond = new DNSMonitorCondition();
		cond.setPort(53);
		cond.setCurrValue("value");
		DNSParameter param = new DNSParameter();
		param.setCondition(cond);
		assertEquals(param.getConditions().size(), 1);
		
		String json = param.encode();
		DNSParameter param2 = new DNSParameter(json);
		assertEquals(param2.getConditions().size(), 1);
	}
}
