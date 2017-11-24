package com.broada.carrier.monitor.method.cli.entity;

import java.util.Properties;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

/**
 * CLI连接访问参数
 * 
 * @author
 *
 */
public class CLIMonitorMethodOption extends MonitorMethod {
	private static final long serialVersionUID = 1L;
  
	public static final String TYPE_ID = "ProtocolCcli";

	public CLIMonitorMethodOption() {

	}

	public CLIMonitorMethodOption(MonitorMethod method) {
		super(method);
	}

	public String getTerminalType() {
		return getProperties().get("terminalType", "");
	}

	public void setTerminalType(String terminalType) {
		getProperties().set("terminalType", terminalType);
	}

	public String getAgentName() {
		return getProperties().get("agentName", "");		
	}

	public void setAgentName(String agentName) {
		getProperties().set("agentName", agentName);
	}

	public String getLoginPrompt() {
		return getProperties().get("loginPrompt", "login:");
	}

	public void setLoginPrompt(String loginPrompt) {
		getProperties().set("loginPrompt", loginPrompt);
	}

	public String getPasswdPrompt() {
		return getProperties().get("passwdPrompt", "Password:");
	}

	public void setPasswdPrompt(String passwdPrompt) {
		getProperties().set("passwdPrompt", passwdPrompt);
	}

	public String getLoginName() {
		return getProperties().get("loginName", "");		
	}

	public void setLoginName(String loginName) {
		getProperties().set("loginName", loginName);
	}

	public int getLoginTimeout() {
		return getProperties().get("loginTimeout", 10000);		
	}

	public void setLoginTimeout(int loginTimeout) {
		getProperties().set("loginTimeout", loginTimeout);
	}

	public String getPassword() {
		return getProperties().get(CLIConstant.OPTIONS_PASSD, "");
	}

	public void setPassword(String password) {
		getProperties().set("password", password);
	}

	public String getPrompt() {
		return getProperties().get("prompt", "#");
	}

	public void setPrompt(String prompt) {
		getProperties().set("prompt", prompt);
	}

	public int getRemotePort() {
		return getProperties().get("remotePort", 23);
	}

	public void setRemotePort(int remotePort) {
		getProperties().set("remotePort", remotePort);
	}

	public String getSessionName() {
		return getProperties().get("sessionName", "telnet");
	}

	public void setSessionName(String sessionName) {
		getProperties().set("sessionName", sessionName);
	}

	public String getSysname() {
		return getProperties().get("sysname", "");
	}

	public void setSysname(String sysname) {
		getProperties().set("sysname", sysname);
	}

	public String getSysversion() {
		return getProperties().get("sysversion", "");
	}

	public void setSysversion(String sysversion) {
		getProperties().set("sysversion", sysversion);
	}

	public Properties toOptions(String ipAddr) {
		Properties properties = new Properties();
		properties.put(CLIConstant.OPTIONS_OS, getSysname());
		properties.put(CLIConstant.OPTIONS_SESSIONNAME, getSessionName());
		properties.put(CLIConstant.OPTIONS_OSVERSION, getSysversion());
		properties.put(CLIConstant.OPTIONS_LOGINNAME, getLoginName());
		properties.put(CLIConstant.OPTIONS_PASSD, getPassword());
		properties.put(CLIConstant.OPTIONS_PROMPT, getPrompt());
		properties.put(CLIConstant.OPTIONS_REMOTEHOST, ipAddr);
		properties.put(CLIConstant.OPTIONS_REMOTEPORT, new Integer(getRemotePort()));
		properties.put(CLIConstant.OPTIONS_LOGINTIMEOUT, new Integer(getLoginTimeout()));
		properties.put(CLIConstant.OPTIONS_AGENTNAME, getAgentName());
		properties.put(CLIConstant.OPTIONS_LOGINPROMPT, getLoginPrompt());
		properties.put(CLIConstant.OPTIONS_PASSDPROMPT, getPasswdPrompt());
		properties.put(CLIConstant.OPTIONS_TERMINALTYPE, getTerminalType());
		return properties;
	}
}
