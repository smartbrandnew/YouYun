package com.broada.carrier.monitor.method.db2;

import java.sql.SQLException;

import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.common.JdbcMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

/**
 * MonitorMethodOption
 * 
 * @author lixy (lixy@broada.com.cn) Create By 2007-4-3 上午10:35:32
 */
public class DB2MonitorMethodOption extends JdbcMethod {
	private static final long serialVersionUID = 1L;
	public static final String JDBC4DB2MONITORMETHOD = "JDBC";
	public static final String CLI4DB2MONITORMETHOD = "CLI";
	public static final String TYPE_ID = "ProtocolDb2";

	public String getDriverType() {
		return (String) getProperties().getByMethod();
	}

	public void setDriverType(String driverType) {
		getProperties().setByMethod(driverType);
	}

	public DB2MonitorMethodOption() {
		super();
	}

	public DB2MonitorMethodOption(MonitorMethod copy) {
		super(copy);
	}

	public int getPort() {
		return getProperties().getByMethod(50000);
	}

	public void setPort(int port) {
		getProperties().setByMethod(port);
	}

	public String getDbversion() {
		return (String) getProperties().getByMethod();
	}

	public void setDbversion(String Dbversion) {
		getProperties().setByMethod(Dbversion);
	}

	public String getDb() {
		return (String) getProperties().getByMethod();
	}

	public void setDb(String db) {
		getProperties().setByMethod(db);
	}

	public CLIMonitorMethodOption gotCliOption() {
		CLIMonitorMethodOption option = new CLIMonitorMethodOption();
		option.setSysname(getSysname());
		option.setSessionName(getSessionName());
		option.setRemotePort(getRemotePort());
		option.setAgentName(getAgentName());
		option.setSysversion(getSysversion());
		return option;
	}

	public void updateCliOption(CLIMonitorMethodOption options) {
		setSysname(options.getSysname());
		setSessionName(options.getSessionName());
		setRemotePort(options.getRemotePort());
		setAgentName(options.getAgentName());
		setSysversion(options.getSysversion());
	}

	public String getAgentName() {
		return getProperties().getByMethod("");
	}

	public void setAgentName(String agentName) {
		getProperties().setByMethod(agentName);
	}

	public int getRemotePort() {
		return (Integer) getProperties().getByMethod();
	}

	public void setRemotePort(int remotePort) {
		getProperties().setByMethod(remotePort);
	}

	public String getSessionName() {
		return getProperties().getByMethod("");
	}

	public void setSessionName(String sessionName) {
		getProperties().setByMethod(sessionName);
	}

	public String getSysname() {
		return getProperties().getByMethod("");
	}

	public void setSysname(String sysname) {
		getProperties().setByMethod(sysname);
	}

	public String getSysversion() {
		return getProperties().getByMethod("");
	}

	public void setSysversion(String sysversion) {
		getProperties().setByMethod(sysversion);
	}

	public String getOptType() {
		return getProperties().getByMethod(JDBC4DB2MONITORMETHOD);
	}

	public void setOptType(String optType) {
		getProperties().setByMethod(optType);
	}

	/**
	 * 判断数据库版本是否大于10
	 * 
	 * @param version
	 * @return
	 * @throws Exception
	 */
	public boolean ifNewVersion() throws SQLException {
		String version = getDbversion();
		if (version == null)
			throw new SQLException("未能获取数据库版本号，请检查监测协议");
		int index = version.indexOf(".");
		if (index == -1)
			index = version.length();
		return Integer.parseInt(version.substring(0, index)) >= 10;
	}
}
