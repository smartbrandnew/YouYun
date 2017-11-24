package uyun.bat.dashboard.api.service;

import java.util.List;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.Dashwindow;

public interface DashwindowService {

	/**
	 * @param dashwindow
	 * @return
	 */
	Dashwindow createDashwindow(Dashwindow dashwindow);

	/**
	 * @param dashwindow
	 * @return
	 */
	Dashwindow updateDashwindow(Dashwindow dashwindow);

	/**
	 * @param id
	 */
	void deleteDashwindow(Dashwindow dashwindow);

	Dashboard sortDashwindows(Dashboard dashboard);
	
	List<Dashwindow> getDashwindowsByDashId(String id);
}
