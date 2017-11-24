package com.broada.carrier.monitor.method.operationcenter;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class OperationCenterMethodOption extends MonitorMethod {

	private static final long serialVersionUID = 1L;

	public static final String TYPE_ID = "OperationCenter";

	public OperationCenterMethodOption() {
		super();
	}

	public OperationCenterMethodOption(MonitorMethod copy) {
		super(copy);
	}
	
	// OCIp
	public String getOCIp(){
		return getProperties().get("OCIp", "");
	}
	
	// OC端口
	public int getPort(){
		return getProperties().get("port", 0);
	}
	
	// 允许访问的IP(白名单)
	public String getHostIp(){
		return getProperties().get("hostIp", "");
	}
	
	// OPENAPI用户名
	public String getUsername() {
		return getProperties().get("username", "");
	}
	
	// OPENAPI 用户密码
	public String getPassword() {
		return getProperties().get("password", "");
	}
	
	public String getVersion(){
		return getProperties().get("version", "v2.3");
	}

}
