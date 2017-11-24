package uyun.bat.web.impl.service.rest.favourite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.favourite.api.entity.Favourite;
import uyun.bat.web.api.favourite.entity.FavouriteDashboard;
import uyun.bat.web.api.favourite.service.FavouriteDashboardWebService;
import uyun.bat.web.impl.common.entity.TenantConstants;
import uyun.bat.web.impl.common.service.ServiceManager;
 
import uyun.bat.web.impl.common.util.DashboardComparator;

import com.alibaba.dubbo.config.annotation.Service;

@Service(protocol = "rest")
@Path("v2/favourites/dashboards")
public class FavouriteRESTService implements FavouriteDashboardWebService {
	private static final DashboardComparator dashboardComparator = new DashboardComparator();
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<FavouriteDashboard> getMyFavouriteDashboards(@HeaderParam(TenantConstants.COOKIE_USERID) String userId) {
		List<Favourite> favourites = ServiceManager.getInstance().getFavouriteService().getMyFavouriteDashboards(userId);
		List<FavouriteDashboard> favouriteDashboards = new ArrayList<FavouriteDashboard>();
		for (Favourite temp : favourites) {
			FavouriteDashboard f = new FavouriteDashboard();
			f.setId(temp.getDashId());
			f.setUserId(temp.getUserId());
			Dashboard d = ServiceManager.getInstance().getDashboardService().getDashboardById(temp.getDashId());
			if(null == d)
				continue;
			f.setName(d.getName());
			favouriteDashboards.add(f);
		}
		Collections.sort(favouriteDashboards, dashboardComparator);
		return favouriteDashboards;
	}

	@POST
	@Path("create")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public void createFavouriteDashboard(@HeaderParam(TenantConstants.COOKIE_USERID) String userId, Dashboard dashboard) {
		Dashboard d = ServiceManager.getInstance().getDashboardService().getDashboardById(dashboard.getId());
		if (d != null)
			ServiceManager.getInstance().getFavouriteService().createFavouriteDashboard(userId, dashboard);
	}

	@POST
	@Path("delete")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public void deleteFavouriteDashboard(@HeaderParam(TenantConstants.COOKIE_USERID) String userId, Dashboard dashboard) {
		ServiceManager.getInstance().getFavouriteService().deleteFavouriteDashboard(userId, dashboard);
	}

}
