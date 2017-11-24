package uyun.bat.gateway.agent.service.serviceapi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import uyun.bat.gateway.agent.service.api.HealthService;

import com.alibaba.dubbo.config.annotation.Service;

@Service(protocol = "rest-service", delay = 3000)
@Path("v2")
public class HealthRESTService implements HealthService {

    @GET
    @Path("healthcheck/status")
    @Override
    public Response healthCheck() {
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("healthcheck/online")
    @Override
    public Response setOnline() {
        return Response.status(Response.Status.OK).build();

    }

    @GET
    @Path("healthcheck/offline")
    @Override
    public Response setOffline() {
        return Response.status(Response.Status.OK).build();

    }
}
