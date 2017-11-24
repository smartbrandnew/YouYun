package com.broada.carrier.monitor.method.domino;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class DominoMonitorMethodOption extends MonitorMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolDomino";

	public DominoMonitorMethodOption() {
		super();
	}

	public DominoMonitorMethodOption(MonitorMethod copy) {
		super(copy);
	}

	public String getUsername() {
		return getProperties().getByMethod("");		
	}

	public void setUsername(String username) {
		getProperties().setByMethod(username);
	}

	public String getPassword() {
		return getProperties().getByMethod("");
	}

	public void setPassword(String password) {
		getProperties().setByMethod(password);
	}

	public int getPort() {
		return getProperties().getByMethod(63148);
	}

	public void setPort(int port) {
		getProperties().setByMethod(port);
	}

	public String getVersion() {
		return getProperties().get("version", "R7");
	}

	public void setVersion(String version) {
		getProperties().set("version", version);
	}
}
