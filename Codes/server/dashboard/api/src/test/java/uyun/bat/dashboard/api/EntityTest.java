package uyun.bat.dashboard.api;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uyun.bat.common.test.entity.EntityTestUtil;
import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.DashboardCount;
import uyun.bat.dashboard.api.entity.Dashwindow;
import uyun.bat.dashboard.api.entity.LineData;
import uyun.bat.dashboard.api.entity.Request;

public class EntityTest {
	@Test
	public void testDashboard() {
		try {
			Object test = EntityTestUtil.create(Dashboard.class);
			assertTrue(test instanceof Dashboard);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void testDashboardCount() {
		try {
			Object test = EntityTestUtil.create(DashboardCount.class);
			assertTrue(test instanceof DashboardCount);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testDashwindow() {
		try {
			Object test = EntityTestUtil.create(Dashwindow.class);
			assertTrue(test instanceof Dashwindow);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testLineData() {
		try {
			Object test = EntityTestUtil.create(LineData.class);
			assertTrue(test instanceof LineData);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testRequest() {
		try {
			Object test = EntityTestUtil.create(Request.class);
			assertTrue(test instanceof Request);
		} catch (Exception e) {
			assertTrue(false);
		}
	}
}
