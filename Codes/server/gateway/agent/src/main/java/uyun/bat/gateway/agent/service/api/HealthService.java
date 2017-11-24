package uyun.bat.gateway.agent.service.api;

import javax.ws.rs.core.Response;

public interface HealthService {
    Response healthCheck();

    Response setOnline();

    Response setOffline();
}
