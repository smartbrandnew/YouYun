package com.broada.carrier.monitor.method.dm;

import com.broada.carrier.monitor.method.common.JdbcMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

/**
 * 
 * 
 * @author Zhouqa
 * Create By 2016年4月6日 上午11:44:45
 */
public class DmMonitorMethodOption extends JdbcMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolDm";

	public DmMonitorMethodOption() {
		super();
	}

	public DmMonitorMethodOption(MonitorMethod copy) {
		super(copy);
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
		return getProperties().getByMethod(5236);
	}

	public void setPort(int port) {
		getProperties().setByMethod(port);
	}
	
	public String getSid() {
		return getProperties().getByMethod("DMSERVER");
	}
	
	public void setSid(String sid) {
		getProperties().setByMethod(sid);
	}
}
