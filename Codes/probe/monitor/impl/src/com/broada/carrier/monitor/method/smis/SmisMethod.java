package com.broada.carrier.monitor.method.smis;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;


public class SmisMethod extends MonitorMethod {
	public static final String TYPE_ID = "ProtocolSmis";
	private static final long serialVersionUID = 1L;
	
	public SmisMethod() {
		super();
	}

	public SmisMethod(MonitorMethod copy) {
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
		return getProperties().getByMethod(5988);
	}

	public void setPort(int port) {
		getProperties().setByMethod(port);
	}
	
	public String getNamespace() {
		return getProperties().getByMethod("");
	}
	
	public void setNamespace(String namespace) {
		getProperties().setByMethod(namespace);
	}
	
	public String getProtocol() {
		return getProperties().getByMethod("");
	}
	
	public void setProtocol(String protocol) {
		getProperties().setByMethod(protocol);
	}
	
	public String getPrivProtocol() {
		return getProperties().getByMethod("");
	}
	
	public void setPrivProtocol(String privprotocol) {
		getProperties().setByMethod(privprotocol);
	}
	
	public String getPrivIp() {
		return getProperties().getByMethod("");
	}

	public void setPrivIp(String privIp) {
		getProperties().setByMethod(privIp);
	}
}