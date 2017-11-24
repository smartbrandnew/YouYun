package com.broada.carrier.monitor.method.jdbc;

import com.broada.carrier.monitor.method.common.JdbcMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class JdbcMonitorMethodOption extends JdbcMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolDbsql";

	public JdbcMonitorMethodOption() {
	}

	public JdbcMonitorMethodOption(MonitorMethod method) {
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

	public String getDbType() {
		return getProperties().get("dbType", (String) null);
	}

	public void setDbType(String dbType) {
		getProperties().set("dbType", dbType);
	}

	public String getEncoding() {
		return getProperties().getByMethod("GBK");
	}

	public void setEncoding(String encoding) {
		getProperties().setByMethod(encoding);
	}

}
