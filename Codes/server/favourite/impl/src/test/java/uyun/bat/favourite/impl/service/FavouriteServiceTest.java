package uyun.bat.favourite.impl.service;

import junit.framework.TestCase;
import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.favourite.impl.Startup;
import uyun.bat.favourite.impl.facade.FacadeManager;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FavouriteServiceTest extends TestCase {
	private static String userId = UUIDTypeHandler.createUUID();
	private static String id = UUIDTypeHandler.createUUID();

	public FavouriteServiceTest() {
		Startup.getInstance().startup();
	}

	public void test1CreateFavouriteDashboard() {
		List<String> list = new ArrayList<String>();
		list.add(UUIDTypeHandler.createUUID());
		list.add(UUIDTypeHandler.createUUID());
		Dashboard dashboard = new Dashboard();
		dashboard.setId(id);
		dashboard.setName("System-Overview1");
		dashboard.setDashwindowIdList(list);
		dashboard.setModified(new Date());
		dashboard.setTenantId("1");
		dashboard.setType("timeseries");
		dashboard.setUserId("admin");
		FacadeManager.getInstance().getFavouriteFacade().createFavouriteDashboard(userId, dashboard);
		id = dashboard.getId();
		assertTrue(id.length() > 0);
	}

	public void test2DeleteFavouriteDashboard() {
		Dashboard dashboard = new Dashboard();
		dashboard.setId(id);
		FacadeManager.getInstance().getFavouriteFacade().deleteFavouriteDashboard(userId, dashboard);
		assertTrue(true);
	}

	public void test3GetMyFavouriteDashboards() {
		FacadeManager.getInstance().getFavouriteFacade().getMyFavouriteDashboards(userId);
		assertTrue(true);
	}

}
