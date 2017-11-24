package com.broada.carrier.monitor.method.common;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class HttpMethod extends BaseMethod {
	private static final long serialVersionUID = 1L;
	
	public HttpMethod() {
		super();
	}

	public HttpMethod(MonitorMethod copy) {
		super(copy);
	}

	public String getProtocol() {
		return getProperties().getByMethod("http");
	}

	public void setProtocol(String protocol) {
		getProperties().setByMethod(protocol);
	}

	public int getPort() {
		return getProperties().getByMethod(80);
	}

	public void setPort(int port) {
		getProperties().setByMethod(port);
	}

	public int getTimeout() {
		return getProperties().getByMethod(5000);
	}

	public void setTimeout(int timeout) {
		getProperties().setByMethod(timeout);
	}

	public String getUsername() {
		return (String) getProperties().getByMethod();
	}

	public void setUsername(String username) {
		getProperties().setByMethod(username);
	}

	public String getPassword() {
		return (String) getProperties().getByMethod();
	}

	public void setPassword(String password) {
		getProperties().setByMethod(password);
	}

}
