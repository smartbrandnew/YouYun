package com.broada.carrier.monitor.probe.impl.method.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLITestResult;
import com.broada.carrier.monitor.method.cli.CLITester;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.probe.api.client.ProbeServiceFactory;
import com.broada.carrier.monitor.probe.impl.util.StringUtils;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;

/**
 * 预先装载系统版本信息
 * @author WIN
 *
 */

public class CliMethodGenerator {

	private static final Logger logger = LoggerFactory.getLogger(CliMethodGenerator.class);
	private static CliMethodGenerator instance = new CliMethodGenerator();

	@Autowired
	private ProbeServiceFactory probeFactory;

	public static CliMethodGenerator getInstance() {
		return instance;
	}

	public CLIMonitorMethodOption getOptions(MonitorNode node, String fileName, String sessionName, String sysName,
			int remotePort, String loginName, String password, String prompt, int loginTimeout) {

		if (StringUtils.isNullOrBlank(sessionName)) {
			logger.warn("配置文件" + fileName + "的collect_methods标签中sessionName的值不能为空");
			return null;
		}
		if (StringUtils.isNullOrBlank(sysName)) {
			logger.warn("配置文件" + fileName + "的collect_methods标签中sysName的值不能为空");
			return null;
		}
		CLIMonitorMethodOption properties = new CLIMonitorMethodOption();
		properties.setSessionName(sessionName);
		properties.setSysname(sysName);
		properties.setRemotePort(remotePort);

		if (sessionName.equalsIgnoreCase(CLIConstant.SESSION_TELNET)
				|| sessionName.equalsIgnoreCase(CLIConstant.SESSION_SSH)) {
			if (StringUtils.isNullOrBlank(loginName)) {
				logger.warn("配置文件" + fileName + "的collect_methods标签中loginName的值不能为空");
				return null;
			}
			if (StringUtils.isNullOrBlank(password)) {
				logger.warn("配置文件" + fileName + "的collect_methods标签中password的值不能为空");
				return null;
			}
			if (remotePort <= 0) {
				logger.warn("请确定配置文件" + fileName + "的collect_methods标签中remotePort值是否合法");
				return null;
			}
			properties.setLoginName(loginName);
			properties.setPassword(password);
			if (StringUtils.isNullOrBlank(prompt))
				prompt = "#";
			properties.setPrompt(prompt);
			if (loginTimeout == 0)
				loginTimeout = 10000;
			// 设置超时时间 ms
			properties.setLoginTimeout(loginTimeout);
			if ("AIX".equalsIgnoreCase(sysName))
				properties.setPasswdPrompt("root's Password:");
			else
				properties.setPasswdPrompt("Password:");
			properties.setLoginPrompt("login:");
		} else if (sessionName.equalsIgnoreCase(CLIConstant.SESSION_WMI)) {
			if (StringUtils.isNullOrBlank(loginName)) {
				logger.warn("配置文件" + fileName + "的collect_methods标签中loginName的值不能为空");
				return null;
			}
			if (StringUtils.isNullOrBlank(password)) {
				logger.warn("配置文件" + fileName + "的collect_methods标签中password的值不能为空");
				return null;
			}
			properties.setLoginName(loginName);
			properties.setPassword(new String(password));
		} else if (sessionName.equalsIgnoreCase(CLIConstant.SESSION_AGENT)) {

		}
		getSySversion(node, properties);
		return properties;
	}

	public CLIMonitorMethodOption getSySversion(MonitorNode node, CLIMonitorMethodOption option) {
		CLITestResult result = null;
		try {
			result = (CLITestResult) probeFactory.getSystemService().executeMethod(CLITester.class.getName(), "doTest",
					node, option);
		} catch (Exception e) {
			logger.warn("获取远程主机系统版本失败,请检查配置是否有效:", e);
		}
		if (result != null) {
			Object errorMessage = result.getError();
			if (errorMessage != null && !errorMessage.equals("")) {
				logger.warn("获取远程主机系统版本失败,失败原因:{}", errorMessage);
			} else {
				String version = result.getVersion();
				if (StringUtils.isNotNullAndTrimBlank(version))
					option.setSysversion(version);
			}
		}
		logger.debug("获取远程主机系统版本成功:{} ", option.getSysversion());
		return option;
	}
}
