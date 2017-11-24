package uyun.bat.dashboard.impl.logic;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.dao.DuplicateKeyException;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.DashboardCount;
import uyun.bat.dashboard.impl.dao.DashboardDao;

public class DashboardLogic {
	@Resource
	private DashboardDao dashboardDao;

	public List<Dashboard> searchDashboardByName(String tenantId, String name, int limit) {
		return dashboardDao.searchDashboardByName(tenantId, name, limit);
	}

	public Dashboard getDashboardById(String id) {
		return dashboardDao.getDashboardById(id);
	}

	public Dashboard getDashboardByName(String name, String tenantId) {
		return dashboardDao.getDashboardByName(name, tenantId);
	}

	public Dashboard createDashboard(Dashboard dashboard) {
		try {
			dashboardDao.create(dashboard);
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("Dashboard name already exist！");
		}
		return dashboard;
	}

	public Dashboard updateDashboard(Dashboard dashboard) {
		dashboardDao.update(dashboard);
		return dashboard;
	}

	public boolean deleteDashboard(Dashboard dashboard) {
		return dashboardDao.delete(dashboard);
	}

	public Dashboard getSimpleDashboardById(String id) {
		return dashboardDao.getDashboardById(id);
	}

	public List<DashboardCount> getDashboardCountByDate(Date startTime, Date endTime) {
		return dashboardDao.getDashboardCountByDate(startTime, endTime);
	}

	public List<DashboardCount> getDashboardCount() {
		return dashboardDao.getDashboardCount();
	}

	public Dashboard getDashboardByTemplateName(String templateName) {
		return dashboardDao.getDashboardByTemplateName(templateName);
	}

}
