package com.broada.carrier.monitor.common.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestObjectUtil {

	@Test
	public void test() {
		Object result = ObjectUtil.executeMethod(String.class.getName(), "format", "hello, %s! %d", new Object[]{"world", 5});
		assertEquals("hello, world! 5", result);
	}

}
