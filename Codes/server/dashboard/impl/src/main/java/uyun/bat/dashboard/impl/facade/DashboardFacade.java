package uyun.bat.dashboard.impl.facade;

import java.util.Date;
import java.util.List;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.DashboardCount;
import uyun.bat.dashboard.impl.logic.LogicManager;

public class DashboardFacade {

	public List<Dashboard> searchDashboardByName(String tenantId, String name, int limit) {
		return LogicManager.getInstance().getDashboardLogic().searchDashboardByName(tenantId, name, limit);
	}

	public Dashboard getDashboardById(String id) {
		return LogicManager.getInstance().getDashboardLogic().getDashboardById(id);
	}

	public Dashboard getDashboardByName(String name, String tenantId) {
		return LogicManager.getInstance().getDashboardLogic().getDashboardByName(name, tenantId);
	}

	public Dashboard createDashboard(Dashboard dashBoard) {
		checkDashboard(dashBoard);
		// 如果是创建内置模板则用户id和租户id不做校验
		boolean isNotTemplate = null == dashBoard.getTemplate() || dashBoard.getTemplate() == false;
		if (isNotTemplate && (dashBoard.getUserId() == null || dashBoard.getUserId().length() == 0))
			throw new IllegalArgumentException("Illegal parameter of dashboard：User Id can't be null");
		if (dashBoard.getModified() == null)
			throw new IllegalArgumentException("Illegal parameter of dashboard：Modified date can't be null");
		return LogicManager.getInstance().getDashboardLogic().createDashboard(dashBoard);
	}

	private void checkDashboard(Dashboard dashBoard) {
		if (dashBoard == null)
		throw new IllegalArgumentException("Illegal parameter of dashboard：dashboard can't be Null");
		if (dashBoard.getId() == null || dashBoard.getId().length() == 0)
		throw new IllegalArgumentException("Illegal parameter of dashboard：dashboard ID can't be null");
		boolean isNotTemplate = null == dashBoard.getTemplate() || dashBoard.getTemplate() == false;
		if (isNotTemplate && (dashBoard.getTenantId() == null || dashBoard.getTenantId().length() == 0))
		throw new IllegalArgumentException("Illegal parameter of dashboard：Tenant ID can't be null");
	}

	public Dashboard updateDashboard(Dashboard dashBoard) {
		checkDashboard(dashBoard);
		if (dashBoard.getModified() == null)
			throw new IllegalArgumentException("Illegal parameter of dashboard：Modified date can't be null");
		return LogicManager.getInstance().getDashboardLogic().updateDashboard(dashBoard);
	}

	public void deleteDashboard(Dashboard dashBoard) {
		if ("".equals(dashBoard.getId()))
			throw new IllegalArgumentException();
		LogicManager.getInstance().getDashboardLogic().deleteDashboard(dashBoard);
	}

	public List<DashboardCount> getDashboardCountByDate(Date startTime, Date endTime) {
		return LogicManager.getInstance().getDashboardLogic().getDashboardCountByDate(startTime, endTime);
	}

	public List<DashboardCount> getDashboardCount() {
		return LogicManager.getInstance().getDashboardLogic().getDashboardCount();
	}
	
	public Dashboard getDashboardByTemplateName(String templateName) {
		return LogicManager.getInstance().getDashboardLogic().getDashboardByTemplateName(templateName);
	}

}
