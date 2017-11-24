package com.broada.carrier.monitor.method.vmware;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;


public class VSphereMonitorMethodOption extends MonitorMethod {
	public static final String TYPE_ID = "ProtocolVmware";
	private static final long serialVersionUID = 1L;
	
	public VSphereMonitorMethodOption() {
		super();
	}

	public VSphereMonitorMethodOption(MonitorMethod copy) {
		super(copy);
	}

	public String getHost() {
		return getProperties().getByMethod("");
	}

	public void setHost(String host) {
		getProperties().setByMethod(host);
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
		return getProperties().getByMethod(443);
	}

	public void setPort(int port) {
		getProperties().setByMethod(port);
	}
}