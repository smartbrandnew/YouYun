package com.broada.carrier.monitor.server.api.service;


public interface ServerSystemService extends BaseSystemService {
	String login(String username, String password);
	
	void logout(String token);	
	
	int getLicenseUsedQuota(String moduleId);
}
