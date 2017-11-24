package uyun.bat.web.api.dashboard.service;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.Dashwindow;

public interface DashwindowWebService {

	/**
	 * 创建仪表窗
	 * 
	 * @param dashwindow
	 * @return
	 */
	Dashwindow createDashwindow(Dashwindow dashwindow);

	/**
	 * 更新仪表窗
	 * 
	 * @param dashwindow
	 * @return
	 */
	Dashwindow updateDashwindow(Dashwindow dashwindow);

	/**
	 * 删除仪表窗
	 * 
	 * @param dashwindow
	 */
	void deleteDashwindow(Dashwindow dashwindow);

	/**
	 * 对仪表窗进行排序
	 * 
	 * @param dashboard
	 * @return
	 */
	Dashboard sortDashwindows(String tenantId, Dashboard dashboard);
}
