package uyun.bat.web.impl.service.rest.dashboard;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.Dashwindow;
import uyun.bat.web.impl.Startup;
import uyun.bat.web.impl.common.service.ServiceManager;
import uyun.bat.web.impl.testservice.DashbrodServiceTest;
import uyun.bat.web.impl.testservice.DashwindowServiceTest;
import uyun.bat.web.impl.testservice.FavouriteServiceTest;
import uyun.bat.web.impl.testservice.StartService;

public class DashwindowRESTServiceTest extends StartService{
	DashwindowRESTService dashwindowREST =new DashwindowRESTService();
	private static final String DASH_ID="94baaadca64344d2a748dff88fe7159e";
	private static final String ID="94baaadca64344d2a748dff88fe7159e";
	Dashwindow dashwindow=new Dashwindow();
	
	@Test
	public void test1CreateDashwindow() {
		dashwindow.setDashId(DASH_ID);
		dashwindow.setId(ID);
		Dashwindow dash=dashwindowREST.createDashwindow(dashwindow);
		assertTrue(dash.getDashId().equals("94baaadca64344d2a748dff88fe7159e")==true);
	}

	@Test
	public void test2UpdateDashwindow() {
		dashwindow.setDashId(DASH_ID);
		dashwindow.setId(ID);
		Dashwindow dash=dashwindowREST.updateDashwindow(dashwindow);
		assertTrue(dash.getDashId().equals("94baaadca64344d2a748dff88fe7159e")==true);
	}

	@Test
	public void test3DeleteDashwindow() {
		dashwindow.setDashId(DASH_ID);
		dashwindow.setId(ID);
		dashwindowREST.deleteDashwindow(dashwindow);
	}

	@Test
	public void test4SortDashwindows() {
		Dashboard dashboard=new Dashboard();
		dashboard =dashwindowREST.sortDashwindows(DASH_ID, dashboard);
		assertTrue(dashboard.getTenantId().equals("94baaadca64344d2a748dff88fe7159e"));
	}

}
