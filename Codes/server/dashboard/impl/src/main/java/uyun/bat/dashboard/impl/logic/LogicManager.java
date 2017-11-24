package uyun.bat.dashboard.impl.logic;

public abstract class LogicManager {
	private static LogicManager instance = new LogicManager() {
	};

	public static LogicManager getInstance() {
		return instance;
	}

	private DashboardLogic dashboardLogic;
	private DashwindowLogic dashwindowLogic;
	private TenantResTemplateLogic tenantResTemplateLogic;
	public TenantResTemplateLogic getTenantResTemplateLogic() {
		return tenantResTemplateLogic;
	}

	public void setTenantResTemplateLogic(TenantResTemplateLogic tenantResTemplateLogic) {
		this.tenantResTemplateLogic = tenantResTemplateLogic;
	}

	public DashboardLogic getDashboardLogic() {
		return dashboardLogic;
	}

	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}

	public DashwindowLogic getDashwindowLogic() {
		return dashwindowLogic;
	}

	public void setDashwindowLogic(DashwindowLogic dashwindowLogic) {
		this.dashwindowLogic = dashwindowLogic;
	}

}
