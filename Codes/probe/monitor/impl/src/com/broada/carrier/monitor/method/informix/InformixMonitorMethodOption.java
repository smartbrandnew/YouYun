package com.broada.carrier.monitor.method.informix;

import com.broada.carrier.monitor.method.common.JdbcMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

/**
 * @author lixy (lixy@broada.com.cn) Create By 2008-5-30 下午05:08:00
 */
public class InformixMonitorMethodOption extends JdbcMethod { 
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolInformix";

	public InformixMonitorMethodOption() {
		super();
	}

	public InformixMonitorMethodOption(MonitorMethod copy) {
		super(copy);
	}

	/**
	 * @return port
	 */
	public int getPort() {
		return getProperties().getByMethod(1526);
	}

	/**
	 * @param port
	 */
	public void setPort(int port) {
		getProperties().setByMethod(port);
	}


	/**
	 * @return servername
	 */
	public String getServername() {
		return  getProperties().get("servername") != null?getProperties().get("servername").toString():
			getProperties().get("serverName").toString();
	}

	/**
	 * @param servername
	 */
	public void setServername(String servername) {
		getProperties().set("servername", servername);
	}

	/**
	 * @return dbname
	 */
	public String getDbname() {
		return getProperties().get("dbname", (String)null);
	}

	/**
	 * @param dbname
	 */
	public void setDbname(String dbname) {
		getProperties().set("dbname", dbname);
	}
}
