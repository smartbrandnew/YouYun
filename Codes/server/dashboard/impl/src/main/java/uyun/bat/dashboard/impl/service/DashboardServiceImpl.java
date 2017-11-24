package uyun.bat.dashboard.impl.service;

import java.util.Date;
import java.util.List;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.DashboardCount;
import uyun.bat.dashboard.api.service.DashboardService;
import uyun.bat.dashboard.impl.facade.FacadeManager;

import com.alibaba.dubbo.config.annotation.Service;

@Service(protocol = "dubbo")
public class DashboardServiceImpl implements DashboardService {

	public List<Dashboard> searchDashboardByName(String tenantId, String name, int limit) {

		return FacadeManager.getInstance().getDashboardFacade().searchDashboardByName(tenantId, name, limit);
	}

	public Dashboard getDashboardById(String dashboardid) {
		return FacadeManager.getInstance().getDashboardFacade().getDashboardById(dashboardid);
	}

	public Dashboard getDashboardByName(String name, String tenantId) {
		return FacadeManager.getInstance().getDashboardFacade().getDashboardByName(name, tenantId);
	}

	public Dashboard createDashboard(Dashboard dashboard) {
		return FacadeManager.getInstance().getDashboardFacade().createDashboard(dashboard);
	}

	public Dashboard updateDashboard(Dashboard dashboard) {
		return FacadeManager.getInstance().getDashboardFacade().updateDashboard(dashboard);
	}

	public void deleteDashboard(Dashboard dashboard) {
		FacadeManager.getInstance().getDashboardFacade().deleteDashboard(dashboard);
	}

	public List<DashboardCount> getDashboardCountByDate(Date startTime, Date endTime) {
		return FacadeManager.getInstance().getDashboardFacade().getDashboardCountByDate(startTime, endTime);
	}

	public List<DashboardCount> getDashboardCount() {
		return FacadeManager.getInstance().getDashboardFacade().getDashboardCount();
	}

	public Dashboard getDashboardByTemplateName(String templateName) {
		return FacadeManager.getInstance().getDashboardFacade().getDashboardByTemplateName(templateName);
	}
}