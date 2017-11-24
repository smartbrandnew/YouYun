package uyun.bat.web.impl.service.rest.dashboard;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.alibaba.dubbo.config.annotation.Service;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.Dashwindow;
import uyun.bat.web.api.dashboard.service.DashwindowWebService;
import uyun.bat.web.impl.common.entity.TenantConstants;
import uyun.bat.web.impl.common.service.ServiceManager;
 
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

@Service(protocol = "rest")
@Path("v2/dashboards/dashwindows")
public class DashwindowRESTService implements DashwindowWebService {

	@POST
	@Path("create")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Dashwindow createDashwindow(Dashwindow dashwindow) {
		dashwindow.setId(UUIDTypeHandler.createUUID());
		return ServiceManager.getInstance().getDashwindowService().createDashwindow(dashwindow);
	}

	@POST
	@Path("update")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Dashwindow updateDashwindow(Dashwindow dashwindow) {
		return ServiceManager.getInstance().getDashwindowService().updateDashwindow(dashwindow);
	}

	@POST
	@Path("delete")
	public void deleteDashwindow(Dashwindow dashwindow) {
		ServiceManager.getInstance().getDashwindowService().deleteDashwindow(dashwindow);
		// HttpServletResponse response =
		// RpcContext.getContext().getResponse(HttpServletResponse.class);
		// response.getWriter().write(s);
	}

	@POST
	@Path("record")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Dashboard sortDashwindows(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId, Dashboard dashboard) {
		 
		dashboard.setTenantId(tenantId);
		return ServiceManager.getInstance().getDashwindowService().sortDashwindows(dashboard);

	}

}