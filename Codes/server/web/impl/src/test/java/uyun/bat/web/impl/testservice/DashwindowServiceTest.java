package uyun.bat.web.impl.testservice;

import java.util.ArrayList;
import java.util.List;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.Dashwindow;
import uyun.bat.dashboard.api.entity.Request;
import uyun.bat.dashboard.api.service.DashwindowService;

public class DashwindowServiceTest implements DashwindowService {

	@Override
	public Dashwindow createDashwindow(Dashwindow dashwindow) {
		// TODO Auto-generated method stub
		System.out.println("创建仪表成功"+dashwindow.getId());
		return dashwindow;
	}

	@Override
	public Dashwindow updateDashwindow(Dashwindow dashwindow) {
		// TODO Auto-generated method stub
		System.out.println("更新仪表成功");
		return dashwindow;
	}

	@Override
	public void deleteDashwindow(Dashwindow dashwindow) {
		// TODO Auto-generated method stub
		System.out.println("删除dashwindow"+dashwindow.toString());
	}

	@Override
	public Dashboard sortDashwindows(Dashboard dashboard) {
		// TODO Auto-generated method stub
		System.out.println("仪表盘排序："+dashboard.getId());
		return dashboard;
	}

	@Override
	public List<Dashwindow> getDashwindowsByDashId(String id) {

		List<Dashwindow> list = new ArrayList<Dashwindow>();

		Dashwindow dash = new Dashwindow();
		dash.setDashId("1231");
		dash.setId("1231");
		dash.setName("name");
		Request request = new Request();
		request.setQ("q");
		List<Request>requests = new ArrayList<>();
		requests.add(request);
		dash.setRequests(requests);
		dash.setLineDatas(null);
		dash.setViz(id);
		list.add(dash);

		return list;
	}

}
