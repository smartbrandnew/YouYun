package com.broada.carrier.monitor.method.websphere;

import java.io.File;
import java.io.IOException;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.utils.TextUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author lixy (lixy@broada.com.cn) Create By 2008-6-2 下午02:33:26
 */
public class WASMonitorMethodOption extends MonitorMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolWSAgent";

	public WASMonitorMethodOption() {
		super();
	}

	public WASMonitorMethodOption(MonitorMethod copy) {
		super(copy);
	}

	public String getConnectorType() {
		return getProperties().getByMethod("SOAP");
	}

	public void setConnectorType(String connectorType) {
		getProperties().setByMethod(connectorType);
	}

	public int getConnectorPort() {
		return getProperties().getByMethod(8880);
	}

	public void setConnectorPort(int connector_port) {
		getProperties().setByMethod(connector_port);
	}

	/**
	 * @return chkAuth
	 */
	@JsonIgnore
	public boolean isChkAuth() {
		return !TextUtil.isEmpty(getUsername());
	}

	/**
	 * @return chkDomain
	 */
	@JsonIgnore
	public boolean isChkDomain() {
		return !TextUtil.isEmpty(getDomain());
	}

	/**
	 * @return domain
	 */
	public String getDomain() {
		return getProperties().getByMethod("");
	}

	/**
	 * @param domain
	 */
	public void setDomain(String domain) {
		getProperties().setByMethod(domain);
	}

	/**
	 * @return passWord
	 */
	public String getPassword() {
		return getProperties().getByMethod("");
	}

	/**
	 * @param passWord
	 */
	public void setPassword(String passWord) {
		getProperties().setByMethod(passWord);
	}

	/**
	 * @return port
	 */
	public int getPort() {
		return getProperties().getByMethod(9080);
	}

	public String getConnectorHost() {
		return getProperties().getByMethod("");
	}

	public void setConnectorHost(String conector_host) {
		getProperties().setByMethod(conector_host);
	}

	/**
	 * @param port
	 */
	public void setPort(int port) {
		getProperties().setByMethod(port);
	}

	/**
	 * @return userName
	 */
	public String getUsername() {
		return getProperties().getByMethod("");
	}

	/**
	 * @param userName
	 */
	public void setUsername(String userName) {
		getProperties().setByMethod(userName);
	}

	@JsonIgnore
	public boolean isChkSSL() {
		return !TextUtil.isEmpty(getServerCerPath());
	}

	public String getServerCerPath() {
		return getProperties().getByMethod("");
	}

	public void setServerCerPath(String serverCerPath) {
		getProperties().setByMethod(serverCerPath);
	}

	public String getClientKeyPath() {
		return getProperties().getByMethod("");
	}

	public void setClientKeyPath(String clientKeyPath) {
		getProperties().setByMethod(clientKeyPath);
	}

	public String getClientKeyPwd() {
		return getProperties().getByMethod("");
	}

	public void setClientKeyPwd(String clientKeyPwd) {
		getProperties().setByMethod(clientKeyPwd);
	}

	public String getServerCerFilePath() {
		return getProperties().getByMethod("");
	}

	public void setServerCerFilePath(String serverCerFilePath) {
		getProperties().setByMethod(serverCerFilePath);
	}
}
