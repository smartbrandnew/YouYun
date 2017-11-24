package uyun.bat.dashboard.impl.service;

import java.util.List;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.Dashwindow;
import uyun.bat.dashboard.api.service.DashwindowService;
import uyun.bat.dashboard.impl.facade.FacadeManager;

import com.alibaba.dubbo.config.annotation.Service;

@Service(protocol = "dubbo")
public class DashwindowServiceImpl implements DashwindowService {

	public Dashwindow createDashwindow(Dashwindow dashwindow) {
		return FacadeManager.getInstance().getDashwindowFacade().createDashwindow(dashwindow);
	}

	public Dashwindow updateDashwindow(Dashwindow dashwindow) {
		return FacadeManager.getInstance().getDashwindowFacade().updateDashwindow(dashwindow);
	}

	public void deleteDashwindow(Dashwindow dashwindow) {
		FacadeManager.getInstance().getDashwindowFacade().deleteDashwindow(dashwindow);
	}

	public Dashboard sortDashwindows(Dashboard dashboard) {
		return FacadeManager.getInstance().getDashwindowFacade().sortDashwindows(dashboard);
	}
	
	public List<Dashwindow> getDashwindowsByDashId(String id){
		return FacadeManager.getInstance().getDashwindowFacade().getDashwindowsByDashId(id);
	}
}