package com.broada.carrier.monitor.probe.impl.method.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.method.db2.DB2MonitorMethodOption;
import com.broada.carrier.monitor.method.db2.DB2Tester;
import com.broada.carrier.monitor.probe.api.client.ProbeServiceFactory;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
/**
 * 预先装载DB2 版本信息
 * @author WIN
 *
 */
public class DB2MethodGenerator {
	private static final Logger logger = LoggerFactory.getLogger(DB2MethodGenerator.class);
	private static DB2MethodGenerator instance = new DB2MethodGenerator();

	@Autowired
	private ProbeServiceFactory probeFactory;

	public static DB2MethodGenerator getInstance() {
		return instance;
	}

	public DB2MonitorMethodOption getDB2MonitorMethodOption(MonitorNode node, DB2MonitorMethodOption method) {
		if ("CLI".equalsIgnoreCase(method.getOptType()))
			return getByCli(node, method);
		else
			return getByJDBC(node, method);
	}

	private DB2MonitorMethodOption getByCli(MonitorNode node, DB2MonitorMethodOption method) {
		String result = "";
		try {
			result = (String) probeFactory.getSystemService().executeMethod(DB2Tester.class.getName(), "doTestCli",
					node.getIp(), "CLI", method.getSysname(), method.getSessionName(),
					String.valueOf(method.getRemotePort()), method.getSysversion(), method.getDb(),
					method.getUsername());
			String[] arrRes = result.split(",");
			if (Boolean.valueOf(arrRes[0])) {
				method.setDbversion(arrRes[1]);
				logger.info("成功连接远程数据库，获取db2版本信息: {}", arrRes[1]);
			}
			return method;
		} catch (Exception e) {
			logger.warn("CLI获取远程DB2数据库版本信息失败: ", e);
		}

		return null;
	}

	private DB2MonitorMethodOption getByJDBC(MonitorNode node, DB2MonitorMethodOption method) {
		String result = "";
		try {
			result = (String) probeFactory.getSystemService().executeMethod(DB2Tester.class.getName(), "doTest",
					method.getDriverType(), node.getIp(), method.getPort(), method.getDb(), method.getUsername(),
					method.getPassword());
			String[] arrRes = result.split(",");
			if (Boolean.valueOf(arrRes[0])) {
				method.setDbversion(arrRes[1]);
				logger.debug("成功连接远程数据库，获取db2版本信息: {}", arrRes[1]);
			}
			return method;
		} catch (Exception e) {
			logger.warn("JDBC获取远程DB2获取远程DB2数据库版本信息失败: ", e);
		}
		return null;

	}

}
