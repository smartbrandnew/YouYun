package uyun.bat.monitor.impl.facade;


/**
 * facade管理者
 * facede层主要对方法参数进行校验
 */
public abstract class FacadeManager {
	private static FacadeManager instance = new FacadeManager() {
	};

	public static FacadeManager getInstance() {
		return instance;
	}

	private MonitorFacade monitorFacade;

	public MonitorFacade getMonitorFacade() {
		return monitorFacade;
	}

	public void setMonitorFacade(MonitorFacade monitorFacade) {
		this.monitorFacade = monitorFacade;
	}

}
