package uyun.bat.favourite.impl.dao;

import java.util.List;

import uyun.bat.favourite.api.entity.Favourite;

public interface FavouriteDao {

	List<Favourite> getMyFavouriteDashboards(String userId);

	void createFavouriteDashboard(String userId, String id);

	void deleteFavouriteDashboard(String userId, String id);
}
