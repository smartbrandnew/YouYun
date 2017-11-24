package uyun.bat.dashboard.impl.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.DashboardCount;
import uyun.bat.dashboard.impl.Startup;
import uyun.bat.dashboard.impl.facade.FacadeManager;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DashboardServiceTest extends TestCase {
	private static String id;
	private static String tenantId = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

	@Override
	protected void setUp() throws Exception {
		Startup.getInstance().startup();
	}

	public void test1CreateDashboard() {
		List<String> list = new ArrayList<String>();
		list.add(UUIDTypeHandler.createUUID());
		list.add(UUIDTypeHandler.createUUID());
		Dashboard dashboard = new Dashboard();
		dashboard.setId(UUIDTypeHandler.createUUID());
		dashboard.setName("System-Overview122");
		dashboard.setTenantId(tenantId);
		dashboard.setDashwindowIdList(list);
		dashboard.setModified(new Date());
		dashboard.setType("timeseries");
		dashboard.setUserId(UUIDTypeHandler.createUUID());
		dashboard.setCreateTime(new Date());
		FacadeManager.getInstance().getDashboardFacade().createDashboard(dashboard);
		id = dashboard.getId();
		assertTrue(id.length() > 0);
	}

	public void test2GetDashboardById() {
		FacadeManager.getInstance().getDashboardFacade().getDashboardById(id);
		assertTrue(true);
	}

	public void test3UpdateDashboard() {
		Dashboard dashboard = new Dashboard();
		dashboard.setId(id);
		dashboard.setName("aaa");
		dashboard.setTenantId(tenantId);
		dashboard.setModified(new Date());
		FacadeManager.getInstance().getDashboardFacade().updateDashboard(dashboard);
		assertTrue(true);
	}

	public void test4SearchDashboardByName() {
		List<Dashboard> list = FacadeManager.getInstance().getDashboardFacade().searchDashboardByName(tenantId, "aaa", 10);
		assertTrue(list != null && list.size() > 0);
	}

	public void test5DeleteDashboard() {
		Dashboard dashboard = new Dashboard();
		dashboard.setId(id);
		dashboard.setTenantId(tenantId);
		FacadeManager.getInstance().getDashboardFacade().deleteDashboard(dashboard);
		assertTrue(true);
	}

	public void test6GetDashboardCountByDate() {
		Date endTime = new Date();
		List<DashboardCount> map = FacadeManager.getInstance().getDashboardFacade()
				.getDashboardCountByDate(new Date(endTime.getTime() - 1000 * 60 * 60 * 24 * 30l), endTime);
		assertTrue(map != null && map.size() >= 0);
	}

	public void test7GetDashboardCount() {
		List<DashboardCount> map = FacadeManager.getInstance().getDashboardFacade().getDashboardCount();
		assertTrue(map != null && map.size() >= 0);
	}
}
