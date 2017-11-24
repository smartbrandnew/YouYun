package uyun.bat.gateway.agent;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uyun.bat.common.test.entity.EntityTestUtil;

public class EntityTest {
	@Test
	public void test() {
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.gateway.agent.entity");
			assertTrue(true);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
