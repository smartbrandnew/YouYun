package com.broada.carrier.monitor.method.st;

import com.broada.carrier.monitor.method.common.JdbcMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class ShentongMethod extends JdbcMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolShentong";

	public ShentongMethod() {
	}

	public ShentongMethod(MonitorMethod method) {
		super(method);
	}

	public String getSid() {
		return getProperties().getByMethod("OSRDB");
	}
	
	public void setSid(String sid) {
		getProperties().setByMethod(sid);
	}
	
	public int getPort() {
		return getProperties().getByMethod(2003);
	}
	
	public void setPort(int port) {
		getProperties().setByMethod(port);
	}
}
