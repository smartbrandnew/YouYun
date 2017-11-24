package com.broada.carrier.monitor.method.fusionmanager;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class FusionManagerMethod extends MonitorMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolFusionManager";
	private String username;
	private String password;
	private int port;

	public FusionManagerMethod() {

	}

	public FusionManagerMethod(MonitorMethod copy) {
		super(copy);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "FusionManagerMethod [username=" + username + ", password=" + password + ", port=" + port + "]";
	}

	

}
