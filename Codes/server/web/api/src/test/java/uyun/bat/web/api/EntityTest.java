package uyun.bat.web.api;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uyun.bat.common.test.entity.EntityTestUtil;

public class EntityTest {
	@Test
	public void testAgent() {
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.web.api.agent");
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testAgentconfig() {
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.web.api.agentconfig");
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testCommon() {
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.web.api.common");
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testDashboard() {
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.web.api.dashboard");
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testEvent() {
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.web.api.event");
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testFavourite() {
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.web.api.favourite");
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testMetric() {
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.web.api.metric");
			assertTrue(true);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void testMonitor() {
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.web.api.monitor");
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testReference() {
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.web.api.reference");
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testResource() {
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.web.api.resource");
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testState() {
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.web.api.state");
			assertTrue(true);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
