package uyun.bat.dashboard.impl.dao;

import java.util.Date;
import java.util.List;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.DashboardCount;

public interface DashboardDao {

	/**
	 * @param name
	 * @return
	 */
	List<Dashboard> searchDashboardByName(String tenantId, String name, int limit);

	Dashboard getDashboardById(String id);

	Dashboard getDashboardByName(String name, String tenantId);

	boolean create(Dashboard dashBoard);

	boolean update(Dashboard dashBoard);

	boolean delete(Dashboard dashBoard);

	List<DashboardCount> getDashboardCountByDate(Date startTime, Date endTime);

	List<DashboardCount> getDashboardCount();

	Dashboard getDashboardByTemplateName(String templateName);
}
