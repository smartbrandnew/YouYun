package uyun.bat.dashboard.impl.logic;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.Dashwindow;
import uyun.bat.dashboard.impl.dao.DashwindowDao;

public class DashwindowLogic {
	@Resource
	private DashwindowDao dashwindowDao;

	public Dashwindow createDashwindow(Dashwindow dashwindow) {
		dashwindowDao.createDashwindow(dashwindow);
		Dashboard dashboard = LogicManager.getInstance().getDashboardLogic().getSimpleDashboardById(dashwindow.getDashId());
		if (dashboard.getDashwindowIdList() == null)
			dashboard.setDashwindowIdList(new ArrayList<String>());
		dashboard.getDashwindowIdList().add(dashwindow.getId());
		LogicManager.getInstance().getDashboardLogic().updateDashboard(dashboard);
		return dashwindow;
	}

	public Dashwindow updateDashwindow(Dashwindow dashwindow) {
		dashwindowDao.updateDashwindow(dashwindow);
		return dashwindow;
	}

	public boolean deleteDashwindow(Dashwindow dashwindow) {
		Dashboard dashboard = LogicManager.getInstance().getDashboardLogic().getSimpleDashboardById(dashwindow.getDashId());
		if (null != dashboard && null != dashboard.getDashwindowIdList()) {
			dashboard.getDashwindowIdList().remove(dashwindow.getId());
			LogicManager.getInstance().getDashboardLogic().updateDashboard(dashboard);
		}
		return dashwindowDao.deleteDashwindow(dashwindow.getId());
	}

	public List<Dashwindow> getDashwindowsByDashId(String id) {
		return dashwindowDao.getDashwindowsByDashId(id);
	}

	public Dashboard sortDashwindows(Dashboard dashboard) {
		return LogicManager.getInstance().getDashboardLogic().updateDashboard(dashboard);
	}
}