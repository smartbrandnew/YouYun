package uyun.bat.favourite.impl.facade;

import java.util.List;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.favourite.api.entity.Favourite;
import uyun.bat.favourite.impl.logic.LogicManager;

public class FavouriteFacade {

	public List<Favourite> getMyFavouriteDashboards(String userId) {
		return LogicManager.getInstance().getFavouriteLogic().getMyFavouriteDashboards(userId);
	}

	public void createFavouriteDashboard(String userId, Dashboard dashboard) {
		LogicManager.getInstance().getFavouriteLogic().createFavouriteDashboard(userId, dashboard);
	}

	public void deleteFavouriteDashboard(String userId, Dashboard dashboard) {
		LogicManager.getInstance().getFavouriteLogic().deleteFavouriteDashboard(userId, dashboard);
	}

}