package com.broada.carrier.monitor.method.cli.entity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.broada.carrier.monitor.common.util.Base64Util;

public class TestCLIMonitorMethodOption {
	@Test 
	public void test() {
		CLIMonitorMethodOption option = new CLIMonitorMethodOption();
		option.setSessionName("wmi");
		String text = Base64Util.encodeObject(option);
		
		CLIMonitorMethodOption other = (CLIMonitorMethodOption) Base64Util.decodeObject(text);
		assertEquals(option.getSessionName(), other.getSessionName());
	}
}
