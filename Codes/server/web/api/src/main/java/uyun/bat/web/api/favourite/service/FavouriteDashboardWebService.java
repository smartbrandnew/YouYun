package uyun.bat.web.api.favourite.service;

import java.util.List;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.web.api.favourite.entity.FavouriteDashboard;

public interface FavouriteDashboardWebService {

	/**
	 * 通过用户ID获取仪表盘收藏列表
	 * 
	 * @param userId 用户ID
	 * @return Favourite
	 */
	List<FavouriteDashboard> getMyFavouriteDashboards(String userId);

	/**
	 * 通过用户ID和dashboardID收藏仪表盘
	 * 
	 * @param userId 用户ID
	 * @param dashboard
	 */
	void createFavouriteDashboard(String userId, Dashboard dashboard);

	/**
	 * 通过用户ID和dashboardId删除仪表盘
	 * 
	 * @param userId
	 * @param dashboard
	 */
	void deleteFavouriteDashboard(String userId, Dashboard dashboard);

}
