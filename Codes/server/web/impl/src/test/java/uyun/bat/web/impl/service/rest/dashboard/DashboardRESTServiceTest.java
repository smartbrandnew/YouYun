package uyun.bat.web.impl.service.rest.dashboard;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.web.api.dashboard.entity.MineDashboard;
import uyun.bat.web.api.dashboard.entity.RichDashboardMetadata;
import uyun.bat.web.impl.Startup;
import uyun.bat.web.impl.common.service.ServiceManager;
import uyun.bat.web.impl.testservice.DashbrodServiceTest;
import uyun.bat.web.impl.testservice.DashwindowServiceTest;
import uyun.bat.web.impl.testservice.EventServiceTest;
import uyun.bat.web.impl.testservice.FavouriteServiceTest;
import uyun.bat.web.impl.testservice.MetricMetaDataServiceTest;
import uyun.bat.web.impl.testservice.MetricServiceTest;
import uyun.bat.web.impl.testservice.MonitorServiceTest;
import uyun.bat.web.impl.testservice.ResourceServiceTest;
import uyun.bat.web.impl.testservice.StartService;
import uyun.bat.web.impl.testservice.StateServiceTest;
import uyun.bat.web.impl.testservice.TagServiceTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DashboardRESTServiceTest extends StartService{
	private static final String TENANT_ID = "94baaadca64344d2a748dff88fe7159e";
	private static final String USER_ID = "94baaadca64344d2a748dff88fe7159e";
	DashboardRESTService dashboard= new DashboardRESTService();
	

	@Test
	public void test1SearchDashboardByName() {
		List<RichDashboardMetadata> list = dashboard.searchDashboardByName(USER_ID, TENANT_ID, "zhou1", 10);
		assertTrue(list != null && list.size() != 0);
	}

	@Test
	public void test2GetDashboardById() {
		MineDashboard mine = dashboard.getDashboardById(USER_ID, TENANT_ID);
//		assertTrue(mine==null);
	}

	@Test
	public void test3CreateDashboard() {
		Dashboard dash =new Dashboard();
		dash.setDescr("This is a test!");
		dash=dashboard.createDashboard(USER_ID, TENANT_ID, dash);
		assertTrue(dash.getDescr()!=null);
	}

	@Test
	public void test4UpdateDashboard() {
		Dashboard dash = new Dashboard();
		dash.setDescr("This is the Updata test!");
		dash=dashboard.updateDashboard(TENANT_ID, dash);
		assertTrue(dash.getDescr().equals("This is the Updata test!"));
	}

	@Test
	public void test5DeleteDashboard() {
		Dashboard dash = new Dashboard();
		dashboard.deleteDashboard(USER_ID, TENANT_ID, dash);
	}

	@Test
	public void test6IsDashboardExist() {
		dashboard.isDashboardExist(TENANT_ID);
	}
	
	@Test
	public void testCopyDashboard(){
		dashboard.copyDashboard(USER_ID, TENANT_ID, "123", "testdashboard");
	}
}
