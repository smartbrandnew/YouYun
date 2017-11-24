package com.broada.carrier.monitor.method.cli.entity;

import java.util.Properties;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class WmiMonitorMethodOption extends MonitorMethod {
	private static final long serialVersionUID = 1L;
	private final static String OPTIONS_OS = "sysname";
	private final static String OPTIONS_OSVERSION = "sysversion";
	private final static String OPTIONS_AGENTNAME = "agentName";
	private final static String OPTIONS_SESSIONNAME = "protocol";
	private final static String OPTIONS_REMOTEPORT = "port";
	private final static String OPTIONS_LOGINNAME = "username";
	private final static String OPTIONS_PASSD = "password";
	public static final String TYPE_ID = "ProtocolWmi";

	public WmiMonitorMethodOption() {

	}

	public WmiMonitorMethodOption(MonitorMethod method) {
		super(method);
	}

	public String getAgentName() {
		return getProperties().get(OPTIONS_AGENTNAME, "");
	}

	public void setAgentName(String agentName) {
		getProperties().set(OPTIONS_AGENTNAME, agentName);
	}

	public String getLoginName() {
		return getProperties().get(OPTIONS_LOGINNAME, "");
	}

	public void setLoginName(String loginName) {
		getProperties().set(OPTIONS_LOGINNAME, loginName);
	}

	public String getPassword() {
		return getProperties().get(OPTIONS_PASSD, "");
	}

	public void setPassword(String password) {
		getProperties().set(OPTIONS_PASSD, password);
	}

	public int getRemotePort() {
		return getProperties().get(OPTIONS_REMOTEPORT, 23);
	}

	public void setRemotePort(int remotePort) {
		getProperties().set(OPTIONS_REMOTEPORT, remotePort);
	}

	public String getSessionName() {
		return getProperties().get(OPTIONS_SESSIONNAME, "telnet");
	}

	public void setSessionName(String sessionName) {
		getProperties().set(OPTIONS_SESSIONNAME, sessionName);
	}

	public String getSysname() {
		return getProperties().get(OPTIONS_OS, "");
	}

	public void setSysname(String sysname) {
		getProperties().set(OPTIONS_OS, sysname);
	}

	public String getSysversion() {
		return getProperties().get(OPTIONS_OSVERSION, "");
	}

	public void setSysversion(String sysversion) {
		getProperties().set(OPTIONS_OSVERSION, sysversion);
	}

	public Properties toOptions(String ipAddr) {
		Properties properties = new Properties();
		properties.put(CLIConstant.OPTIONS_OS, getSysname());
		properties.put(CLIConstant.OPTIONS_SESSIONNAME, getSessionName());
		properties.put(CLIConstant.OPTIONS_OSVERSION, getSysversion());
		properties.put(CLIConstant.OPTIONS_LOGINNAME, getLoginName());
		properties.put(CLIConstant.OPTIONS_PASSD, getPassword());
		properties.put(CLIConstant.OPTIONS_REMOTEHOST, ipAddr);
		properties.put(CLIConstant.OPTIONS_REMOTEPORT, new Integer(getRemotePort()));
		properties.put(CLIConstant.OPTIONS_AGENTNAME, getAgentName());

		return properties;
	}
}
