package com.broada.carrier.monitor.method.ldap;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class LdapMethod extends MonitorMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolLdap";

	public LdapMethod() {
	}

	public LdapMethod(MonitorMethod copy) {
		super(copy);
	}

	public int getPort() {
		return getProperties().getByMethod(389);
	}

	public void setPort(int port) {
		getProperties().setByMethod(port);
	}

	public int getVersion() {
		return getProperties().get("version", 3);
	}

	public void setVersion(int version) {
		getProperties().set("version", version);
	}

	public String getBaseDN() {
		return getProperties().getByMethod("");
	}

	public void setBaseDN(String baseDN) {
		getProperties().setByMethod(baseDN);
	}

	public boolean isAnonymous() {
		return getProperties().getByMethod(false);
	}

	public void setAnonymous(boolean anonymous) {
		getProperties().setByMethod(anonymous);
	}

	public boolean isAppendBaseDN() {
		return getProperties().getByMethod(false);
	}

	public void setAppendBaseDN(boolean appendBaseDN) {
		getProperties().setByMethod(appendBaseDN);
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

}
