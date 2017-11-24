package com.broada.carrier.monitor.impl.db.oracle.util;

/**
 * 连接参数，用于连接池
 * @author Jiangjw
 */
public class ConnectionParameter {
	private String url;
	private String user;
	private String password;

	/**
	 * 构造函数
	 * @param url
	 * @param user
	 * @param password
	 */
	public ConnectionParameter(String url, String user, String password) {
		super();
		this.url = url;
		this.user = user;
		this.password = password;
	}

	/**
	 * url
	 * @return
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * 用户名
	 * @return
	 */
	public String getUser() {
		return user;
	}

	/**
	 * 密码
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	@Override
	public int hashCode() {
		return url.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		ConnectionParameter another = (ConnectionParameter) obj;
		return this.url.equals(another.url) && this.user.equals(another.user) && this.password.equals(another.password);
	}

	@Override
	public String toString() {
		return String.format("%s[%s %s:%s]", getClass().getSimpleName(), url, user, password);
	}
}
