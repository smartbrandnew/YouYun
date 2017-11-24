package uyun.bat.web.impl.testservice;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.DashboardCount;
import uyun.bat.dashboard.api.service.DashboardService;

public class DashbrodServiceTest implements DashboardService {

	@Override
	public List<Dashboard> searchDashboardByName(String tenantId, String name, int limit) {
		// TODO Auto-generated method stub
		List<Dashboard> list=new ArrayList<Dashboard>();
		Dashboard dash1 = new Dashboard();
		dash1.setId("123");
		dash1.setName("Monitor");
		dash1.setTenantId("123");
		Dashboard dash2 = new Dashboard();
		dash2.setId("123");
		dash2.setName("数据监测");
		dash2.setTenantId("123");
		Dashboard dash3 = new Dashboard();
		dash3.setId("123");
		dash3.setName("openstack");
		dash3.setTenantId("123");
		list.add(dash1);
		list.add(dash2);
		list.add(dash3);
		return list;
	}

	@Override
	public Dashboard getDashboardById(String id) {
		// TODO Auto-generated method stub
		Dashboard dashboard = new Dashboard();
		dashboard.setId("123"+1);
		dashboard.setName(id);
		dashboard.setTenantId(id);
		dashboard.setModified(new Date());
		return dashboard;
	}

	@Override
	public Dashboard getDashboardByName(String name, String tenantId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dashboard createDashboard(Dashboard dashboard) {
		System.out.println("创建仪表盘成功"+dashboard.getId());
		return dashboard;
	}

	@Override
	public Dashboard updateDashboard(Dashboard dashboard) {
		// TODO Auto-generated method stub
		return dashboard;
	}

	@Override
	public void deleteDashboard(Dashboard dashboard) {
		// TODO Auto-generated method stub
		System.out.println("删除dashboard:"+dashboard.getName());
	}

	@Override
	public List<DashboardCount> getDashboardCountByDate(Date startTime, Date endTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DashboardCount> getDashboardCount() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dashboard getDashboardByTemplateName(String templateName) {
		if(templateName.equals("app")){
			return null;
		}
		Dashboard dashboard = new Dashboard();
		dashboard.setId("1231");
		dashboard.setName("name");
		List<String>dashwindowIdList = new ArrayList<>();
		dashwindowIdList.add("1231");
		dashboard.setDashwindowIdList(dashwindowIdList);
		return dashboard;
	}

}