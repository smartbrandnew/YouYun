package uyun.bat.web.impl.service.rest.status;

import com.alibaba.dubbo.config.annotation.Service;
import uyun.bat.web.api.status.HealthService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Service(protocol = "rest")
@Path("v2/healthcheck")
public class HealthRESTService implements HealthService{
    @GET
    @Path("status")
    @Override
    public Response healthCheck() {
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("online")
    @Override
    public Response setOnline() {
        return Response.status(Response.Status.OK).build();

    }

    @GET
    @Path("offline")
    @Override
    public Response setOffline() {
        return Response.status(Response.Status.OK).build();

    }
}
