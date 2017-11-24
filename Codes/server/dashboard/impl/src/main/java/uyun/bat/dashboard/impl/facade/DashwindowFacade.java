package uyun.bat.dashboard.impl.facade;

import java.util.List;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.Dashwindow;
import uyun.bat.dashboard.impl.logic.LogicManager;

public class DashwindowFacade {

	public Dashwindow createDashwindow(Dashwindow dashwindow) {
		return LogicManager.getInstance().getDashwindowLogic().createDashwindow(dashwindow);
	}

	public Dashwindow updateDashwindow(Dashwindow dashwindow) {
		if ("".equals(dashwindow.getId()))
			throw new IllegalArgumentException();
		return LogicManager.getInstance().getDashwindowLogic().updateDashwindow(dashwindow);
	}

	public void deleteDashwindow(Dashwindow dashwindow) {
		if ("".equals(dashwindow.getId()))
			throw new IllegalArgumentException();
		LogicManager.getInstance().getDashwindowLogic().deleteDashwindow(dashwindow);
	}

	public List<Dashwindow> getDashwindowsByDashId(String id) {
		return LogicManager.getInstance().getDashwindowLogic().getDashwindowsByDashId(id);
	}

	public Dashboard sortDashwindows(Dashboard dashboard) {
		//bat-845 多用户在Monitor添加图表时将其他仪表清除 
		//根据dashboard id查询获取所有的dashwindows列表
		Dashboard temp = FacadeManager.getInstance().getDashboardFacade().getDashboardById(dashboard.getId());
		if(temp==null)
			/*throw new IllegalArgumentException("查询返回为空");*/
			return null;
		List<String> tempList=temp.getDashwindowIdList();
		//根据dashwindows列表个数和dashboard的列表进行对比
		if(tempList.size()!=dashboard.getDashwindowIdList().size()){
			throw new IllegalArgumentException("Errors occur when verify the dashwindows of this dashboard");
		}
		//校验dashwindows列表的各个id是否与dashboard相一致
		tempList.removeAll(dashboard.getDashwindowIdList());
		if(tempList.isEmpty()){
			return LogicManager.getInstance().getDashwindowLogic().sortDashwindows(dashboard);
		}else{
			throw new IllegalArgumentException("Errors occur when verify the dashwindows of this dashboard");
		}
	}
}