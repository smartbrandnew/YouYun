package com.broada.carrier.monitor.method.oracle;

import com.broada.carrier.monitor.method.common.JdbcMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class OracleRACMethod extends JdbcMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolOracleRAC";

	public OracleRACMethod() {
	}

	public OracleRACMethod(MonitorMethod method) {
		super(method);
	}

	public String getSid() {
		return getProperties().getByMethod("orcl");
	}
	
	public void setSid(String sid) {
		getProperties().setByMethod(sid);
	}
	
	public int getPort() {
		return getProperties().getByMethod(1521);
	}
	
	public void setPort(int port) {
		getProperties().setByMethod(port);
	}
}
