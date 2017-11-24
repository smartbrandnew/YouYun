package com.broada.carrier.monitor.impl.generic.script.context;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.session.TSCLISession;
import com.broada.carrier.monitor.method.cli.session.ssh.SshCLISession;
import com.broada.carrier.monitor.method.cli.session.telnet.TelnetCLISession;
import com.broada.numen.agent.script.context.Context;

public class NetCLIContext implements Context {
	private static final Log logger = LogFactory.getLog(NetCLIContext.class);
	
	public static final String CLI_TYPE_SSH = "ssh";
	public static final String CLI_TYPE_TELNET = "telnet";
	public static final int CLI_PORT_SSH = 22;
	public static final int CLI_PORT_TELNET = 23;
	public static final int CLI_PORT_AUTO = 0;
	public static final int CLI_TIMEOUT = 15000;
	public static final String CLI_PROMPT_LOGIN = "ogin:";
	public static final String CLI_PROMPT_PASSD = "assword:";
	public static final String CLI_PROMPT = "$";
	public static final String CLI_TERMINAL_TYPE = "dumb";

	private String type = CLI_TYPE_TELNET;
	private String targetAddr;
	private int targetPort = CLI_PORT_AUTO;
	private int timeout = CLI_TIMEOUT;
	private String username;
	private String password;
	private String loginPrompt = CLI_PROMPT_LOGIN;
	private String passwordPrompt = CLI_PROMPT_PASSD;
	private String prompt = CLI_PROMPT;
	private String terminalType = CLI_TERMINAL_TYPE;
	private TSCLISession session;
	private StringBuffer console = new StringBuffer();
	
	private static String checkParam(String name, String value) {
		if (value == null || value.length() == 0)
			throw new IllegalArgumentException(String.format("参数[%s]不可以为空", name));
		return value;
	}
	
	private static boolean isStringValid(String str) {
		return str != null && str.trim().length() > 0;		
	}

	private static void usage() {
		logger.info("Usage");
		System.exit(-1);
	}

	/**
	 * 根据当前设置值进行连接
	 * @throws CLIConnectException 
	 * @throws CLILoginFailException 
	 */
	public void connect() throws CLILoginFailException, CLIConnectException {
		if (session != null)
			throw new IllegalStateException("连接失败，当前已经处于连接状态。" + this);
		
		Properties opts = new Properties();
  	opts.put("passwdPrompt", checkParam("密码提示符", passwordPrompt));
  	opts.put("loginPrompt", checkParam("登录提示符", loginPrompt));
  	opts.put("prompt", checkParam("命令提示符", prompt));
  	opts.put("sessionName", checkParam("连接协议", type));
  	opts.put("remoteHost", checkParam("连接目标地址", targetAddr));
  	opts.put("agentName", "");
  	opts.put("terminalType", checkParam("终端类型", terminalType));
  	int port;
  	if (targetPort == CLI_PORT_AUTO) {
  		if (type.equals(CLI_TYPE_SSH))
  			port = CLI_PORT_SSH;
  		else if (type.equals(CLI_TYPE_TELNET))
  			port = CLI_PORT_TELNET;
  		else
  			throw new IllegalArgumentException(String.format("未知的连接协议[%s]", type));
  	} else
  		port = targetPort;
  	opts.put("remotePort", port);
  	opts.put("loginName", checkParam("用户名", username));
  	opts.put("password", password);
  	if (timeout <= 0)
  		throw new IllegalArgumentException(String.format("参数超时时间不能小于等于0，目前设置为：%d", timeout));
  	opts.put("loginTimeout", timeout);  	
  	
  	
		if (type.equals(CLI_TYPE_SSH))
			session = new SshCLISession();
		else if (type.equals(CLI_TYPE_TELNET))
			session = new TelnetCLISession();
		else
			throw new IllegalArgumentException(String.format("未知的连接协议[%s]", type));  	
		
		logger.info("尝试建立连接：" + this);
		session.open(opts,true);
	}
	
	/**
	 * 使用默认的TELNET协议进行连接，其它参数也都使用默认值
	 * @param targetAddr
	 * @param username
	 * @param password
	 * @throws CLIConnectException 
	 * @throws CLILoginFailException 
	 */
	public void connect(String targetAddr, String username, String password) throws CLILoginFailException, CLIConnectException {
		setTargetAddr(targetAddr);
		setUsername(username);
		setPassword(password);
		connect();
	}

	/**
	 * 执行命令，并返回结果
	 * @param cmd
	 * @return
	 * @throws CLIException
	 */
	public String execute(String cmd) throws CLIException {
		if (session == null)
			throw new IllegalStateException("执行失败，当前还没有建立连接。" + this);			
		
		StringBuffer sb = new StringBuffer();
		session.execCmd(cmd, null, prompt, sb,true);
		
		console.append(sb);
		
		return sb.toString();
	}

	public String getName() {
		return "netcli";
	}

	public void close() {
		if (session != null)
			session.close();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getLoginPrompt() {
		return loginPrompt;
	}

	public void setLoginPrompt(String loginPrompt) {
		this.loginPrompt = loginPrompt;
	}

	public String getPasswordPrompt() {
		return passwordPrompt;
	}

	public void setPasswordPrompt(String passwordPrompt) {
		this.passwordPrompt = passwordPrompt;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTargetAddr() {
		return targetAddr;
	}

	public void setTargetAddr(String targetAddr) {
		this.targetAddr = targetAddr;
	}

	public int getTargetPort() {
		return targetPort;
	}

	public void setTargetPort(int targetPort) {
		this.targetPort = targetPort;
	}

	@Override
	public String toString() {
		return String.format("网络CLI会话[协议：%s 地址：%s 用户名：%s]", type, targetAddr, username);
	}

	public void setConsole(StringBuffer console) {
		this.console = console;
	}

	public String getTerminalType() {
		return terminalType;
	}

	public void setTerminalType(String terminalType) {
		this.terminalType = terminalType;
	}
}
