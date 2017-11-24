package uyun.bat.dashboard.api.service;

import java.util.Date;
import java.util.List;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.DashboardCount;

public interface DashboardService {

	/**
	 * @param tenantId
	 * @return
	 */
	List<Dashboard> searchDashboardByName(String tenantId, String name, int limit);

	/**
	 * @param id
	 * @return
	 */
	Dashboard getDashboardById(String id);

	/**
	 * @param name
	 * @return
	 */
	Dashboard getDashboardByName(String name, String tenantId);

	/**
	 * @param dashboard
	 * @return
	 */
	Dashboard createDashboard(Dashboard dashboard);

	/**
	 * @param dashboard
	 * @return
	 */
	Dashboard updateDashboard(Dashboard dashboard);

	/**
	 * @param dashboard
	 */
	void deleteDashboard(Dashboard dashboard);

	/**
	 * @param templateId
	 * @return
	 */
	Dashboard getDashboardByTemplateName(String templateName);

	/********************************* 以下接口for门户 *******************************************/

	/**
	 * 查询某段时间内创建仪表盘的数量
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	List<DashboardCount> getDashboardCountByDate(Date startTime, Date endTime);

	/**
	 * 查询全部仪表盘数量
	 * 
	 * @return
	 */
	List<DashboardCount> getDashboardCount();

}
