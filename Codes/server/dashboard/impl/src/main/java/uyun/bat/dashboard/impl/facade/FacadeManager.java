package uyun.bat.dashboard.impl.facade;

public abstract class FacadeManager {
	private static FacadeManager instance = new FacadeManager() {
	};

	public static FacadeManager getInstance() {
		return instance;
	}

	private DashboardFacade dashboardFacade;
	private DashwindowFacade dashwindowFacade;
	private TenantResTemplateFacade tenantResTemplateFacade;
	public TenantResTemplateFacade getTenantResTemplateFacade() {
		return tenantResTemplateFacade;
	}

	public void setTenantResTemplateFacade(TenantResTemplateFacade tenantResTemplateFacade) {
		this.tenantResTemplateFacade = tenantResTemplateFacade;
	}

	public DashboardFacade getDashboardFacade() {
		return dashboardFacade;
	}

	public void setDashboardFacade(DashboardFacade dashboardFacade) {
		this.dashboardFacade = dashboardFacade;
	}

	public DashwindowFacade getDashwindowFacade() {
		return dashwindowFacade;
	}

	public void setDashwindowFacade(DashwindowFacade dashwindowFacade) {
		this.dashwindowFacade = dashwindowFacade;
	}

}
