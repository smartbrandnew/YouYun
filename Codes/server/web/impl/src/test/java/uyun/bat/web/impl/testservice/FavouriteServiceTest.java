package uyun.bat.web.impl.testservice;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.favourite.api.entity.Favourite;
import uyun.bat.favourite.api.service.FavouriteService;

public class FavouriteServiceTest implements FavouriteService{

	@Override
	public List<Favourite> getMyFavouriteDashboards(String userId) {
		// TODO Auto-generated method stub
		List<Favourite> list=new ArrayList<Favourite>();
		Favourite fa =new Favourite();
		fa.setDashId("自习");
		fa.setUserId("mike");
		Favourite fa1 =new Favourite();
		fa1.setDashId("语文");
		fa1.setUserId("jason");
		Favourite fa2 =new Favourite();
		fa2.setDashId("json");
		fa2.setUserId("周");
		Favourite fa3 =new Favourite();
		fa3.setDashId("Zoo");
		fa3.setUserId("Zoo");
		
		list.add(fa);
		list.add(fa1);
		list.add(fa2);
		list.add(fa3);
		return list;
	}

	@Override
	public void createFavouriteDashboard(String userId, Dashboard dashboard) {
		// TODO Auto-generated method stub
		System.out.println("createFavouriteDashboard Success!");
	}

	@Override
	public void deleteFavouriteDashboard(String userId, Dashboard dashboard) {
		// TODO Auto-generated method stub
		System.out.println("deleteFavouriteDashboard Success!");
	}

}
