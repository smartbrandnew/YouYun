package uyun.bat.dashboard.impl.dao;

import java.util.List;

import uyun.bat.dashboard.api.entity.Dashwindow;

public interface DashwindowDao {

	boolean createDashwindow(Dashwindow dashwindow);

	boolean updateDashwindow(Dashwindow dashwindow);

	boolean deleteDashwindow(String id);
	
	List<Dashwindow> getDashwindowsByDashId(String id);
}
