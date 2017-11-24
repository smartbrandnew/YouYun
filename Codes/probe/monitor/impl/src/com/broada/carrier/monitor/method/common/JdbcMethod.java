package com.broada.carrier.monitor.method.common;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class JdbcMethod extends BaseMethod {
	private static final long serialVersionUID = 1L;
	
	public JdbcMethod() {
		super();
	}

	public JdbcMethod(MonitorMethod copy) {
		super(copy);
	}

	public String getDriver() {
		return (String) getProperties().getByMethod();
	}

	public void setDriver(String driver) {
		getProperties().setByMethod(driver);
	}

	public String getUrl() {
		return (String) getProperties().getByMethod();
	}

	public void setUrl(String url) {
		getProperties().setByMethod(url);
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
