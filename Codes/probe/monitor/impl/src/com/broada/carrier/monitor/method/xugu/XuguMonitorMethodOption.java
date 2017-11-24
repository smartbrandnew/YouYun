package com.broada.carrier.monitor.method.xugu;

import com.broada.carrier.monitor.method.common.JdbcMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class XuguMonitorMethodOption extends JdbcMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolXugu";

	public XuguMonitorMethodOption() {
		super();
	}

	public XuguMonitorMethodOption(MonitorMethod copy) {
		super(copy);
	}
	
	public int getPort(){
		return getProperties().get("port", 5138);
	}
	
	public String getDatabaseName(){
		return getProperties().get("database", "SYSTEM");
	}
	
	public String getUserName(){
		return (String) getProperties().get("username");
	}
	
	public String getPassword(){
		return (String) getProperties().get("password");
	}

	public int getConType(){
		return getProperties().get("con_type", 0);
	}
	
	public String getIps(){
		return (String) getProperties().get("ips");
	}
	
	public String getSessionName(){
		return (String) getProperties().get("sessionName");
	}
	
	public int getRemotePort(){
		return getProperties().get("remotePort", 22);
	}
	
	public int getLoginTimeout(){
		return getProperties().get("loginTimeout", 10000);
	}
	
	public String getUsernameForCli(){
		return (String) getProperties().get("usernameCli");
	}
	
	public String getPasswordForCli(){
		return (String) getProperties().get("passwordCli");
	}
	
	public String getPromt(){
		return getProperties().get("prompt", "#");
	}
	
	public String getSysname(){
		return (String) getProperties().get("sysname");
	}
	
	public int getWaitTimeout(){
		return getProperties().get("waitTimeout", 10000);
	}
	
}
