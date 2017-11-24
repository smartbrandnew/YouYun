package uyun.bat.agent.api;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uyun.bat.common.test.entity.EntityTestUtil;

public class EntityTest {
	@Test
	public void testAgent() {
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.agent.api.entity");
			assertTrue(true);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
