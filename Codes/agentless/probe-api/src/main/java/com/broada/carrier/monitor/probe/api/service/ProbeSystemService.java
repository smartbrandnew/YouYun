package com.broada.carrier.monitor.probe.api.service;

import com.broada.carrier.monitor.server.api.service.BaseSystemService;

public interface ProbeSystemService extends BaseSystemService {
	void deleteAll();
	
	void exit(String reason);
}
