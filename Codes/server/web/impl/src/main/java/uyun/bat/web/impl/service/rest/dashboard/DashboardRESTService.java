package uyun.bat.web.impl.service.rest.dashboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.alibaba.dubbo.config.annotation.Service;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.Dashwindow;
import uyun.bat.datastore.api.exception.Illegalargumentexception;
import uyun.bat.datastore.api.util.StringUtils;
import uyun.bat.favourite.api.entity.Favourite;
import uyun.bat.web.api.dashboard.entity.MineDashboard;
import uyun.bat.web.api.dashboard.entity.RichDashboardMetadata;
import uyun.bat.web.api.dashboard.service.DashboardWebService;
import uyun.bat.web.impl.common.entity.TenantConstants;
import uyun.bat.web.impl.common.service.ServiceManager;
 
import uyun.bat.web.impl.common.util.EncryptUtil;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service(protocol = "rest")
@Path("v2/dashboards")
public class DashboardRESTService implements DashboardWebService {
	@GET
	@Path("query")
	@Produces(MediaType.APPLICATION_JSON)
	public List<RichDashboardMetadata> searchDashboardByName(@HeaderParam(TenantConstants.COOKIE_USERID) String userId,
			@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId, @QueryParam("name") String name,
			@QueryParam("limit") @DefaultValue("10") int limit) {

		// TODO: 2017/3/16 对参数进行合理的转义和校验, 保证一下测试用例的返回结果一直:
		// name = ","$2812":"
		// name = ","$query":{},"A":"
		// name = ","$query":{"Non1Existent2Field":3},"A":"
		 

		List<Dashboard> dashboardList = ServiceManager.getInstance().getDashboardService()
				.searchDashboardByName(tenantId, name, limit);
		List<RichDashboardMetadata> richDashboardList = new ArrayList<RichDashboardMetadata>();
		if (dashboardList == null || dashboardList.size() == 0) {
			richDashboardList.add(new RichDashboardMetadata());
			return richDashboardList;
		}
		if (dashboardList != null && dashboardList.size() > 0) {
			List<Favourite> favourites = ServiceManager.getInstance().getFavouriteService().getMyFavouriteDashboards(userId);

			// 收藏提前
			List<RichDashboardMetadata> fDash = new ArrayList<RichDashboardMetadata>();
			List<RichDashboardMetadata> unfDash = new ArrayList<RichDashboardMetadata>();
			for (Dashboard temp : dashboardList) {
				RichDashboardMetadata rich = generateRichDashboard(temp, favourites);
				if (rich.isFavourite()) {
					fDash.add(rich);
				} else {
					unfDash.add(rich);
				}
			}
			richDashboardList.addAll(fDash);
			richDashboardList.addAll(unfDash);
		}
		return richDashboardList;
	}

	private RichDashboardMetadata generateRichDashboard(Dashboard dash, List<Favourite> favourites) {
		if (dash == null)
			return null;
		RichDashboardMetadata rich = new RichDashboardMetadata();
		rich.setId(dash.getId());
		rich.setName(dash.getName());
		if (favourites != null && favourites.size() > 0) {
			for (Favourite f : favourites) {
				if (dash.getId().equals(f.getDashId())) {
					rich.setFavourite(true);
					break;
				}
			}
		}
		return rich;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public MineDashboard getDashboardById(@HeaderParam(TenantConstants.COOKIE_USERID) String userId,
			@QueryParam("dashboardid") String dashboardid) {
		if (!StringUtils.isNotNullAndBlank(dashboardid))
			throw new Illegalargumentexception("dashboardid can't be empty！");
		String regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$";
		if (!dashboardid.matches(regex))
			throw new Illegalargumentexception("Dashboardid is not correct！");
		Dashboard dashboard = ServiceManager.getInstance().getDashboardService().getDashboardById(dashboardid);
		if (dashboard == null)
			return null;
		MineDashboard mineDashboard = new MineDashboard();
		mineDashboard.setId(dashboard.getId());
		mineDashboard.setName(dashboard.getName());
		List<String> records = dashboard.getDashwindowIdList();
		if (records != null && records.size() > 0) {
			List<Dashwindow> dashwindows = ServiceManager.getInstance().getDashwindowService()
					.getDashwindowsByDashId(dashboardid);
			if (dashwindows != null && dashwindows.size() > 0) {
				List<Dashwindow> sortList = new ArrayList<Dashwindow>();
				for (String dashwindowId : records) {
					for (Dashwindow temp : dashwindows) {
						if (temp.getId().equals(dashwindowId)) {
							sortList.add(temp);
							break;
						}
					}
				}
				mineDashboard.setDashwindows(sortList);
			}
		}
		// 该用户是否收藏过 
		List<Favourite> favourites = ServiceManager.getInstance().getFavouriteService().getMyFavouriteDashboards(userId);
		for (Favourite f : favourites) {
			if (dashboardid.equals(f.getDashId()))
				mineDashboard.setFavourite(true);
		}
		return mineDashboard;
	}

	@POST
	@Path("create")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Dashboard createDashboard(@HeaderParam(TenantConstants.COOKIE_USERID) String userId,
			@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId, Dashboard dashboard) {

		// TODO: 2017/3/16 增加参数校验 
		if(dashboard.getName()!=null&&dashboard.getName().length()>=255)
			throw new IllegalArgumentException("dashboard parameter error");
		if(dashboard.getType()!=null&&dashboard.getType().length()>16)
			throw new IllegalArgumentException("dashboard parameter error");
		if(dashboard.getTemplate()!=null&&!(dashboard.getTemplate() instanceof Boolean))
			throw new IllegalArgumentException("dashboard parameter error");
		dashboard.setUserId(userId);
		dashboard.setTenantId(tenantId);
		dashboard.setCreateTime(new Date());
		dashboard.setId(EncryptUtil.string2MD5("[" + dashboard.getName() + "],[" + tenantId + "]"));
		Dashboard newDashboard = ServiceManager.getInstance().getDashboardService().createDashboard(dashboard);
		ServiceManager.getInstance().getFavouriteService().createFavouriteDashboard(userId, newDashboard);
		return newDashboard;
	}

	@POST
	@Path("update")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Dashboard updateDashboard(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId, Dashboard dashboard) {
		 
		dashboard.setTenantId(tenantId);
		// TODO: 2017/3/16 增加参数校验
		if(dashboard.getId()!=null&&dashboard.getId().length()!=32)
			throw new IllegalArgumentException("dashboard parameter error");
		if(dashboard.getName()!=null&&dashboard.getName().length()>=255)
			throw new IllegalArgumentException("dashboard parameter error");
		if(dashboard.getType()!=null&&dashboard.getType().length()>16)
			throw new IllegalArgumentException("dashboard parameter error");
		if(dashboard.getTemplate()!=null&&!(dashboard.getTemplate() instanceof Boolean))
			throw new IllegalArgumentException("dashboard parameter error");
		return ServiceManager.getInstance().getDashboardService().updateDashboard(dashboard);
	}

	@POST
	@Path("delete")
	public void deleteDashboard(@HeaderParam(TenantConstants.COOKIE_USERID) String userId,
			@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId, Dashboard dashboard) {
		dashboard.setTenantId(tenantId);
		ServiceManager.getInstance().getDashboardService().deleteDashboard(dashboard);
		ServiceManager.getInstance().getFavouriteService().deleteFavouriteDashboard(userId, dashboard);
		List<Dashwindow> dashwindows = ServiceManager.getInstance().getDashwindowService()
				.getDashwindowsByDashId(dashboard.getId());
		if (dashwindows != null && dashwindows.size() > 0) {
			for (Dashwindow d : dashwindows) {
				ServiceManager.getInstance().getDashwindowService().deleteDashwindow(d);
			}
		}
	}

	@GET
	@Path("isExist")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean isDashboardExist(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId) {
		 
		int count = ServiceManager.getInstance().getDashboardService().searchDashboardByName(tenantId, "", 10).size();
		if (count > 0)
			return true;
		return false;
	}
	
	@GET
	@Path("copy")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Dashboard copyDashboard(@HeaderParam(TenantConstants.COOKIE_USERID) String userId,
			@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,@QueryParam("dashboardid") String dashboardid,
			@QueryParam("dashboardname") String dashboardName) {

		// TODO: 2017/3/16 增加参数校验 
		
		if(dashboardName!=null&&dashboardName.length()>=255)
			throw new IllegalArgumentException("dashboard parameter error");
		Dashboard dashboard = ServiceManager.getInstance().getDashboardService().getDashboardById(dashboardid);
		if(dashboard==null){
			throw new IllegalArgumentException("No query to the corresponding dashboard");
		}
		if(dashboard.getType()!=null&&dashboard.getType().length()>16)
			throw new IllegalArgumentException("dashboard parameter error");
		if(dashboard.getTemplate()!=null&&!(dashboard.getTemplate() instanceof Boolean))
			throw new IllegalArgumentException("dashboard parameter error");
		
		//拷贝原来的仪表盘
		dashboard.setName(dashboardName);
		dashboard.setUserId(userId);
		dashboard.setTenantId(tenantId);
		dashboard.setCreateTime(new Date());
		dashboard.setId(EncryptUtil.string2MD5("[" + dashboard.getName() + "],[" + tenantId + "]"));
		dashboard.setDashwindowIdList(null);
		//拷贝原有的列表
		List<String> list = new ArrayList<String>();
		List<Dashwindow> dashList = ServiceManager.getInstance().getDashwindowService().getDashwindowsByDashId(dashboardid);
		Dashwindow dashwindow = new Dashwindow();
		Dashboard newDashboard = ServiceManager.getInstance().getDashboardService().createDashboard(dashboard);
		Iterator<Dashwindow> it = dashList.iterator();
		while(it.hasNext()){
			dashwindow = (Dashwindow) it.next();
			dashwindow.setDashId(dashboard.getId());
			dashwindow.setId(UUIDTypeHandler.createUUID());
			list.add(dashwindow.getId());
			ServiceManager.getInstance().getDashwindowService().createDashwindow(dashwindow);
		}
		ServiceManager.getInstance().getFavouriteService().createFavouriteDashboard(userId, newDashboard);
		return newDashboard;
	}
}