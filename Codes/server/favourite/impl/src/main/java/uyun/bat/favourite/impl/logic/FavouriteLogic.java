package uyun.bat.favourite.impl.logic;

import java.util.List;

import javax.annotation.Resource;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.favourite.api.entity.Favourite;
import uyun.bat.favourite.impl.dao.FavouriteDao;

public class FavouriteLogic {
	@Resource
	private FavouriteDao favouriteDao;

	public List<Favourite> getMyFavouriteDashboards(String userId) {
		return favouriteDao.getMyFavouriteDashboards(userId);
	}

	public void createFavouriteDashboard(String userId, Dashboard dashboard) {
		favouriteDao.createFavouriteDashboard(userId, dashboard.getId());
	}

	public void deleteFavouriteDashboard(String userId, Dashboard dashboard) {
		favouriteDao.deleteFavouriteDashboard(userId, dashboard.getId());
	}

}