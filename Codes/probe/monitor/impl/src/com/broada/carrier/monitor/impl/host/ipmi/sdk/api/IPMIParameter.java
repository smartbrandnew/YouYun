package com.broada.carrier.monitor.impl.host.ipmi.sdk.api;

import java.io.Serializable;

/**
 * IPMI访问参数
 * @author Jiangjw
 */
public class IPMIParameter implements Serializable {
	private static final long serialVersionUID = 1L;
	private String host;
	private String user;
	private String password;
	private String level;
	
	public IPMIParameter() {		
	}

	public IPMIParameter(String host, String user, String password, String level) {
		this.host = host;
		this.user = user;
		this.password = password;
		this.level = level;
	}

	public String getHost() {
		return host;
	}

	public String getUsername() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * 用户级别，默认为null，表示administrator
	 * @return
	 */
	public String getLevel() {
		return level;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setUsername(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	@Override
	public String toString() {
		return String.format("BMC IP：%s，用户名：%s", host, user);
	}
}
