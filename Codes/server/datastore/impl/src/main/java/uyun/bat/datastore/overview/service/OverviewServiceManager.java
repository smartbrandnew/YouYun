package uyun.bat.datastore.overview.service;

import uyun.bat.monitor.api.service.MonitorService;

public class OverviewServiceManager {
	private static OverviewServiceManager instance = new OverviewServiceManager();

	private MonitorService monitorService;

	public static OverviewServiceManager getInstance() {
		return instance;
	}

	public MonitorService getMonitorService() {
		return monitorService;
	}

	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}

}
