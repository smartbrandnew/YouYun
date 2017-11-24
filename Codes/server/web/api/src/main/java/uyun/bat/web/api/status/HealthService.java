package uyun.bat.web.api.status;

import javax.ws.rs.core.Response;

public interface HealthService {
    Response healthCheck();

    Response setOnline();

    Response setOffline();
}
