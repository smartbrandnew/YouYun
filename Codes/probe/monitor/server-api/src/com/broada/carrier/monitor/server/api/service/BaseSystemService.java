package com.broada.carrier.monitor.server.api.service;

import java.util.Date;

import com.broada.carrier.monitor.server.api.entity.SystemInfo;

public interface BaseSystemService {
	SystemInfo[] getInfos();
	
	Date getTime();
	
	String getProperty(String code);
	
	Object executeMethod(String className, String methodName, Object... params);
}
