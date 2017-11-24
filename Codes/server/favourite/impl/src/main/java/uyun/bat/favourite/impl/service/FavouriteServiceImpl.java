package uyun.bat.favourite.impl.service;

import java.util.List;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.favourite.api.entity.Favourite;
import uyun.bat.favourite.api.service.FavouriteService;
import uyun.bat.favourite.impl.facade.FacadeManager;

import com.alibaba.dubbo.config.annotation.Service;

@Service(protocol = "dubbo")
public class FavouriteServiceImpl implements FavouriteService {

	public List<Favourite> getMyFavouriteDashboards(String userId) {
		return FacadeManager.getInstance().getFavouriteFacade().getMyFavouriteDashboards(userId);
	}

	public void createFavouriteDashboard(String userId, Dashboard dashboard) {
		FacadeManager.getInstance().getFavouriteFacade().createFavouriteDashboard(userId, dashboard);
	}

	public void deleteFavouriteDashboard(String userId, Dashboard dashboard) {
		FacadeManager.getInstance().getFavouriteFacade().deleteFavouriteDashboard(userId, dashboard);
	}

}
