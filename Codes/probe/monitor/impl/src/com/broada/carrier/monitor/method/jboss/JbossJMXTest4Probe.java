package com.broada.carrier.monitor.method.jboss;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import javax.naming.InitialContext;
import org.apache.commons.httpclient.methods.GetMethod;
import com.broada.carrier.monitor.impl.mw.jboss.JbossHttpGetUtil;

public class JbossJMXTest4Probe {
	public String test(String ipAddr, Integer port, String version, String username, String password) {
		String ret = null;
		if ("4.x".equalsIgnoreCase(version) || "5.x".equalsIgnoreCase(version)) {
			ret = testJboss4And5(ipAddr, port, username, password);
		} else if ("6.x".equals(version)) {
			ret = testJboss6(ipAddr, port, username, password);
		} else if ("6.x-eap".equals(version)) {
			// eap与7.x监测方式基本一致
			ret = testJboss7(ipAddr, port, username, password);
		} else if ("7.x".equalsIgnoreCase(version)) {
			ret = testJboss7(ipAddr, port, username, password);
		}

		return ret;
	}

	private String testJboss6(String ipAddr, int port, String username, String password) {
		try {
			String url = "http://" + ipAddr + ":" + port
					+ "/jmx-console/HtmlAdaptor?action=inspectMBean&name=jboss.system%3Atype%3DServerInfo";
			GetMethod method = JbossHttpGetUtil.getMethod(url, username, password);
			method.getResponseBodyAsString();
			method.releaseConnection();
		} catch (Exception e) {
			return exception2String(e);
		}
		return null;
	}

	private String testJboss7(String ipAddr, int port, String username, String password) {
		try {
			String url = "http://" + ipAddr + ":" + port + "/management/core-service/platform-mbean/type/runtime";
			GetMethod method = JbossHttpGetUtil.getMethod(url, username, password);
			method.getResponseBodyAsString();
			method.releaseConnection();
		} catch (Exception e) {
			return exception2String(e);
		}
		return null;
	}

	private String testJboss4And5(String ipAddr, int port, String username, String password) {

		Properties pro = new Properties();
		pro.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		pro.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
		pro.setProperty("java.naming.security.principal", username);
		pro.setProperty("java.naming.security.credentials", password);
		pro.setProperty("java.naming.provider.url", "jnp://" + ipAddr + ":" + port);
		try {
			InitialContext ic = new InitialContext(pro);
			ic.lookup("jmx/rmi/RMIAdaptor");
		} catch (Exception e) {
			return "测试JBoss连接失败：\n网络不通或者目标主机上的JBoss没有启动。" + exception2String(e);
		}
		return null;
	}

	public static String exception2String(Throwable ex) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bos);
		ex.printStackTrace(ps);
		ps.close();
		return new String(bos.toByteArray());
	}
}
