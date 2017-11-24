package com.broada.carrier.monitor.method.cli.session.agent;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.agent.config.HostAgentClient;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.session.AbstractCLISession;
import com.broada.numen.agent.original.service.OriginalAgent;

public class AgentCLISession extends AbstractCLISession {
	private static final Log logger = LogFactory.getLog(AgentCLISession.class);

	/*
	 * 
	 * @see com.broada.carrier.monitor.method.cli.session.CLISession#execCmd(java.lang.String, java.lang.String[], java.lang.String, java.lang.StringBuffer, boolean)
	 */
	public String execCmd(String cmd, String[] args, String prompt, StringBuffer localBuf, boolean isLogErr)
			throws CLIException {
		if (args != null) {
			for (int index = 0; index < args.length; index++) {
				cmd += " " + args[index];
			}
		}
		OriginalAgent agent = null;
		String ip = options.getProperty(CLIConstant.OPTIONS_REMOTEHOST);
		int port = ((Integer) options.get(CLIConstant.OPTIONS_REMOTEPORT)).intValue();
		String rmiName = "uniagent";
		try {
			agent = HostAgentClient.getHostAgent(ip, port, rmiName);
		} catch (RemoteException e1) {
			throw new CLIException("获取远程代理失败,可能是代理没有开启或者网络无法连通,代理=" + ip + ":" + port + ":" + rmiName, e1);
		} catch (Throwable e1) {
			throw new CLIException("获取远程代理失败,未知异常,代理=" + ip + ":" + port + ":" + rmiName, e1);
		}

		try {
			String result = agent.execCmd(cmd).toString();
			if (result != null) {
				result = result.trim();
				localBuf.append(result);
				if (options.getProperty(CLIConstant.OPTIONS_OS).equalsIgnoreCase("windows")) {
					//由于返回的前三行数据干扰统一解析，为了同wmi一致，过滤掉
					int index = 0;
					int found = 0;
					while (found < 3) {
						index = result.indexOf(CLIConstant.LINE_SPILTTER, index) + CLIConstant.LINE_SPILTTER.length();
						found++;
					}
					result = result.substring(index);
				}
			}
			return result;
		} catch (RemoteException e) {
			logger.error("执行命令[" + cmd + "]时错误,代理=" + ip + ":" + port + ":" + rmiName, e);
			throw new CLIException("执行命令[" + cmd + "]时错误.", e);
		}
	}

	public List<String> runSQL(String cmd, String[] args, StringBuffer localBuf) throws CLIException {
		if (args != null && args.length > 0) {
			// 将脚本中的登录用户名替换成实际的用户
			if (cmd.indexOf("#db2User#") > -1) {
				cmd = cmd.replaceAll("#db2User#", args[0]);
			}
			for (int index = 1; index < args.length; index++) {
				cmd += " \"" + args[index] + "\"";
			}
		}
		OriginalAgent agent = null;
		String ip = options.getProperty(CLIConstant.OPTIONS_REMOTEHOST);
		int port = ((Integer) options.get(CLIConstant.OPTIONS_REMOTEPORT)).intValue();
		String rmiName = "uniagent";
		try {
			agent = HostAgentClient.getHostAgent(ip, port, rmiName);
		} catch (RemoteException e1) {
			throw new CLIException("获取远程代理失败,可能是代理没有开启或者网络无法连通,代理=" + ip + ":" + port + ":" + rmiName, e1);
		} catch (Throwable e1) {
			throw new CLIException("获取远程代理失败,未知异常,代理=" + ip + ":" + port + ":" + rmiName, e1);
		}

		try {
			return agent.runSQL(cmd);
		} catch (RemoteException e) {
			logger.error("执行命令[" + cmd + "]时错误,代理=" + ip + ":" + port + ":" + rmiName, e);
			throw new CLIException("执行命令[" + cmd + "]时错误.", e);
		}

	}

	/*
	 * 
	 * @see com.broada.carrier.monitor.method.cli.session.CLISession#execScript(java.lang.String, java.lang.String[])
	 */
	public String execScript(String scriptFile, String[] args) throws CLIException {
		OriginalAgent agent = null;
		String ip = options.getProperty(CLIConstant.OPTIONS_REMOTEHOST);
		int port = ((Integer) options.get(CLIConstant.OPTIONS_REMOTEPORT)).intValue();
		String rmiName = options.getProperty(CLIConstant.OPTIONS_AGENTNAME);
		try {
			agent = HostAgentClient.getHostAgent(ip, port, rmiName);
		} catch (RemoteException e1) {
			throw new CLIException("获取远程代理失败,可能是代理没有开启或者网络无法连通,代理=" + ip + ":" + port + ":" + rmiName, e1);
		} catch (Throwable e1) {
			throw new CLIException("获取远程代理失败,未知异常,代理=" + ip + ":" + port + ":" + rmiName, e1);
		}

		try {
			return agent.execScript(scriptFile, args).toString();
		} catch (RemoteException e) {
			logger.error("执行脚本[" + scriptFile + "]时错误,代理=" + ip + ":" + port + ":" + rmiName, e);
			throw new CLIException("执行命令[" + scriptFile + "]时错误.", e);
		}
	}

	/*
	 * 
	 * @see com.broada.carrier.monitor.method.cli.session.CLISession#open(java.util.Properties, boolean)
	 */
	public void open(Properties options, boolean isLogErr) throws CLILoginFailException, CLIConnectException {
		this.options = options;
	}

	/*
	 * 
	 * @see com.broada.carrier.monitor.method.cli.session.CLISession#close()
	 */
	public void close() {
		HostAgentClient.removeAgent(options.getProperty(CLIConstant.OPTIONS_REMOTEHOST), ((Integer) (options
				.get(CLIConstant.OPTIONS_REMOTEPORT))).intValue(), (String) (options.get(CLIConstant.OPTIONS_AGENTNAME)));
	}

	/*
	 * 
	 * @see com.broada.carrier.monitor.method.cli.session.AbstractCLISession#hasContext()
	 */
	public boolean hasContext() {
		return false;
	}

	@Override
	public boolean isStanding() {
		return true;
	}

}