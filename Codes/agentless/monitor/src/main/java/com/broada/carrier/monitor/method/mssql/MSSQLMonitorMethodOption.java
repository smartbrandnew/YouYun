package com.broada.carrier.monitor.method.mssql;

import com.broada.carrier.monitor.common.util.TextUtil;
import com.broada.carrier.monitor.method.common.JdbcMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * SQLSERVER参数
 * 
 * @author zhoucy(zhoucy@broada.com.cn) Create By May 5, 2008 10:10:46 AM
 */
public class MSSQLMonitorMethodOption extends JdbcMethod {
	public static final String TYPE_ID = "ProtocolSqlserver";
	private static final long serialVersionUID = 1L;
	
	public MSSQLMonitorMethodOption() {
		super();
	}

	public MSSQLMonitorMethodOption(MonitorMethod copy) {
		super(copy);
	}

	public String getDomain() {
		return getProperties().getByMethod("");
	}

	public void setDomain(String domain) {
		getProperties().setByMethod(domain);
	}

	@JsonIgnore
	public boolean hasDomain() {
		return TextUtil.isEmpty(getDomain());
	}

	public String getInstanceName() {
		return getProperties().getByMethod("");
	}

	public void setInstanceName(String instanceName) {
		getProperties().setByMethod(instanceName);		
	}
	
	public int getPort() {
		return getProperties().getByMethod(1433);
	}
	
	public void setPort(int port) {
		getProperties().setByMethod(port);
	}
}
