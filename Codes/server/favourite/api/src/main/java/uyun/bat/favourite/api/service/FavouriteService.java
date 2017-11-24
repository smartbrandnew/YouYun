package uyun.bat.favourite.api.service;

import java.util.List;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.favourite.api.entity.Favourite;

public interface FavouriteService {

	List<Favourite> getMyFavouriteDashboards(String userId);

	void createFavouriteDashboard(String userId, Dashboard dashboard);

	void deleteFavouriteDashboard(String userId, Dashboard dashboard);

}
