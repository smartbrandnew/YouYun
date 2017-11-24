package com.broada.carrier.monitor.method.weblogic.agent;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class WebLogicJMXOption extends MonitorMethod {	
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolWLAgent";
	
	public WebLogicJMXOption() {
		super();
	}

	public WebLogicJMXOption(MonitorMethod copy) {
		super(copy);
	}

	public String getHost() {
		return (String) getProperties().getByMethod();
	}

	public void setHost(String host) {
		getProperties().setByMethod(host);
	}

	public String getPassword() {
		return (String) getProperties().getByMethod();
	}

	public void setPassword(String password) {
		getProperties().setByMethod(password);
	}

	public int getPort() {
		return getProperties().getByMethod(7001);
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

	public String getAgentName() {
		return (String) getProperties().getByMethod("WebLogicMonitor");
	}

	public void setAgentName(String agentName) {
		getProperties().setByMethod(agentName);
	}

	public int getProxyPort() {
		return getProperties().getByMethod(8001);
	}

	public void setProxyPort(int proxyPort) {
		getProperties().setByMethod(proxyPort);
	}

	public boolean isIfCluster() {
		return getProperties().getByMethod(false);
	}

	public void setIfCluster(boolean isCluster) {
		getProperties().setByMethod(isCluster);
	}
}
