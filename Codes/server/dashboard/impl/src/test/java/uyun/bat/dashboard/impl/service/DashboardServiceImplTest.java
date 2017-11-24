package uyun.bat.dashboard.impl.service;


import java.util.Date;
import org.junit.Test;
import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.Dashwindow;
import uyun.bat.dashboard.impl.Startup;

public class DashboardServiceImplTest {

	private static String TENANT_ID = "94baaadca64344d2a748dff88fe7159e";
	private static String ID = "94baaadca64344d2a748dff88fe7159e";
	private static String TEST_NAME = "testName";
	static{
		Startup.getInstance().startup();
	}
	@Test
	public void test() {
		DashboardServiceImpl dashboardServiceImpl = new DashboardServiceImpl();
		dashboardServiceImpl.searchDashboardByName(TENANT_ID,TEST_NAME,3);
		dashboardServiceImpl.getDashboardById(TENANT_ID);
		dashboardServiceImpl.getDashboardByName(TEST_NAME,TENANT_ID);
		Dashboard dashboard = new Dashboard();
		dashboard.setId(ID);
		dashboard.setUserId(ID);
		Date modified = new Date();
		dashboard.setModified(modified);
		dashboard.setTenantId(TENANT_ID);
		dashboard.setName(TEST_NAME);
		dashboardServiceImpl.updateDashboard(dashboard);
		dashboardServiceImpl.deleteDashboard(dashboard);
		Date startTime = new Date();
		Date endTime = new Date();
		dashboardServiceImpl.getDashboardCountByDate(startTime, endTime);
		dashboardServiceImpl.getDashboardCount();
		dashboardServiceImpl.getDashboardByName(TEST_NAME, TENANT_ID);
		
		DashwindowServiceImpl dashwindowServiceImpl = new DashwindowServiceImpl();
		dashwindowServiceImpl.sortDashwindows(dashboard);
	}
	
	@Test
	public void testDashwindowServiceImpl(){
		DashwindowServiceImpl dashwindowServiceImpl = new DashwindowServiceImpl();
		Dashwindow dashwindow = new Dashwindow();
		dashwindow.setId(ID);
		dashwindow.setName(TEST_NAME);
		dashwindow.setDashId(ID);
		dashwindowServiceImpl.updateDashwindow(dashwindow);
		dashwindowServiceImpl.deleteDashwindow(dashwindow);
		dashwindowServiceImpl.getDashwindowsByDashId(ID);
	}

}
