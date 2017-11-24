package uyun.bat.dashboard.impl.service;

import junit.framework.TestCase;
import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.Dashwindow;
import uyun.bat.dashboard.api.entity.Request;
import uyun.bat.dashboard.impl.Startup;
import uyun.bat.dashboard.impl.facade.FacadeManager;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DashwindowServiceTest extends TestCase {
	private static String id;
	private static String dashId;

	@Override
	protected void setUp() throws Exception {
		Startup.getInstance().startup();
	}

	public void test1CreateDashwindow() {
		List<String> list = new ArrayList<String>();
		list.add(UUIDTypeHandler.createUUID());
		list.add(UUIDTypeHandler.createUUID());
		Dashboard dashboard = new Dashboard();
		dashboard.setId(UUIDTypeHandler.createUUID());
		dashboard.setName("System-Overview122");
		dashboard.setTenantId("1");
		dashboard.setDashwindowIdList(list);
		dashboard.setModified(new Date());
		dashboard.setTenantId(UUIDTypeHandler.createUUID());
		dashboard.setType("timeseries");
		dashboard.setUserId(UUIDTypeHandler.createUUID());
		dashboard.setCreateTime(new Date());
		dashId = FacadeManager.getInstance().getDashboardFacade().createDashboard(dashboard).getId();
		Dashwindow dashwindow = new Dashwindow();
		List<Request> list1 = new ArrayList<Request>();
		Request res = new Request();
		res.setQ("avg:system.load.1{host:张三}");
		res.setType("line");
		res.setAggregator("avg");
		res.setColor("1");
		list1.add(res);
		dashwindow.setId(UUIDTypeHandler.createUUID());
		dashwindow.setName("mywindow");
		dashwindow.setDashId(dashId);
		dashwindow.setRequests(list1);
		dashwindow.setViz("timeseries");
		FacadeManager.getInstance().getDashwindowFacade().createDashwindow(dashwindow);
		id = dashwindow.getId();
		System.out.println(id);
		assertTrue(id.length() > 0);
	}

	public void test2UpdateDashwindow() {
		Dashwindow dashwindow = new Dashwindow();
		dashwindow.setId(id);
		dashwindow.setName("aaa");
		FacadeManager.getInstance().getDashwindowFacade().updateDashwindow(dashwindow);
		assertTrue(true);
	}

	public void test4DeleteDashwindow() {
		Dashwindow dashwindow = new Dashwindow();
		dashwindow.setId(id);
		dashwindow.setDashId(dashId);
		FacadeManager.getInstance().getDashwindowFacade().deleteDashwindow(dashwindow);
		assertTrue(true);
	}

	public void test3SortDashwindows() {
		Dashboard dashboard = new Dashboard();
		List<String> list = new ArrayList<String>();
		/*list.add(UUIDTypeHandler.createUUID());
		list.add(UUIDTypeHandler.createUUID());
		dashboard.setId(dashId);
		dashboard.setDashwindowIdList(list);*/
		dashboard=FacadeManager.getInstance().getDashboardFacade().getDashboardById(dashId);
		list=dashboard.getDashwindowIdList();
		String temp = list.get(0);
		list.set(0, list.get(1));
		list.set(1, temp);
		dashboard.setDashwindowIdList(list);
		FacadeManager.getInstance().getDashwindowFacade().sortDashwindows(dashboard);
		assertTrue(true);
	}
}
