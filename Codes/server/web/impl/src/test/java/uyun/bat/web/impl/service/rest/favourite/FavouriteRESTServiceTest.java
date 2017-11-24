package uyun.bat.web.impl.service.rest.favourite;

import java.util.List;

import org.junit.Test;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.web.api.favourite.entity.FavouriteDashboard;
import uyun.bat.web.impl.testservice.StartService;

public class FavouriteRESTServiceTest extends StartService{
	FavouriteRESTService favoriteREST =new FavouriteRESTService(); 
	private static final String USER_ID="94baaadca64344d2a748dff88fe7159e";
	private static final String T_ID="94baaadca64344d2a748dff88fe7159e";
	Dashboard dashboard =new Dashboard();
	

	
	@Test
	public void testGetMyFavouriteDashboards() {
		List<FavouriteDashboard> list = favoriteREST.getMyFavouriteDashboards(USER_ID);
		for(FavouriteDashboard e : list){
			System.out.println(e.getName());
		}
	}

	@Test
	public void testCreateFavouriteDashboard() {
		dashboard.setId(T_ID);
		favoriteREST.createFavouriteDashboard(USER_ID, dashboard);
	}

	@Test
	public void testDeleteFavouriteDashboard() {
		favoriteREST.deleteFavouriteDashboard(USER_ID, dashboard);
	}

}
