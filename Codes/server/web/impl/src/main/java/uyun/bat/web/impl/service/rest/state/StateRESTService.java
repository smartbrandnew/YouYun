package uyun.bat.web.impl.service.rest.state;

import com.alibaba.dubbo.config.annotation.Service;
import uyun.bat.web.api.state.service.StateWebService;
import uyun.bat.web.impl.common.entity.TenantConstants;
import uyun.bat.web.impl.common.service.ServiceManager;
 

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;


@Service(protocol = "rest")
@Path("v2/states")
public class StateRESTService implements StateWebService{

    @GET
    @Path("apps")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getStateApps(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId) {
         
        List<String> stateMetrics=ServiceManager.getInstance().getStateMetricService().getStateMetrics(tenantId);
        if (null==stateMetrics||stateMetrics.isEmpty()){
            return new ArrayList<>();
        }
        List<String> list=new ArrayList<>();
        for(String metric:stateMetrics){
            int index=metric.lastIndexOf(".");
            if (index==-1){
                continue;
            }
            String app=metric.substring(0,index);
            StringBuilder sb=new StringBuilder();
            sb.append(app);
            sb.append(":");
            sb.append(metric);
            list.add(sb.toString());
        }
        return list;
    }

    @GET
    @Path("tags")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getTagsByState(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
                                       @QueryParam("state") String state) {
         
        return ServiceManager.getInstance().getStateService().getTagsByState(tenantId,state);
    }
}
