package com.broada.carrier.monitor.server.api.client.restful;

import com.broada.carrier.monitor.server.api.client.restful.entity.LoginRequest;
import com.broada.carrier.monitor.server.api.service.ServerSystemService;

public class ServerSystemClient extends BaseSystemClient implements ServerSystemService {
	public ServerSystemClient(String baseServiceUrl) {		
		super(baseServiceUrl, "/api/v1/monitor/system");
	}

	@Override
	public String login(String username, String password) {
		return client.post("sessions", String.class, new LoginRequest(username, password));
	}

	@Override
	public void logout(String token) {
		client.post("sessions/" + token + "/delete");
	}

	@Override
	public int getLicenseUsedQuota(String moduleId) {
		return client.get("license/used-quota/" + moduleId, Integer.class);
	}

}
