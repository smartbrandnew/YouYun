package com.broada.carrier.monitor.method.jboss;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class JbossJMXOption extends MonitorMethod {

	private static final long serialVersionUID = 1L;

	public static final String TYPE_ID = "ProtocolJBoss";

	public JbossJMXOption() {
		super();
	}

	public JbossJMXOption(MonitorMethod copy) {
		super(copy);
	}

	public String getIpAddr() {
		return (String) getProperties().getByMethod();
	}

	public void setIpAddr(String ipAddr) {
		getProperties().setByMethod(ipAddr);
	}

	public String getPassword() {
		return (String) getProperties().getByMethod();
	}

	public void setPassword(String password) {
		getProperties().setByMethod(password);
	}

	public int getPort() {
		return getProperties().getByMethod(1099);
	}

	public void setPort(int port) {
		getProperties().setByMethod(port);
	}

	public String getUsername() {
		return (String) getProperties().getByMethod();
	}

	public void setUsername(String username) {
		getProperties().setByMethod(username);
	}

	public String getVersion() {
		return (String) getProperties().get("version", "4.x");
	}

	public void setVersion(String version) {
		getProperties().set("version", version);
	}
}
