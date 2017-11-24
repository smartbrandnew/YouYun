package com.broada.carrier.monitor.impl.mw.jboss.pmi;

import java.io.IOException;
import java.util.Properties;

import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import com.broada.carrier.monitor.impl.mw.jboss.JbossHttpGetUtil;
import com.broada.carrier.monitor.impl.mw.jboss.JbossRemoteException;
import com.broada.carrier.monitor.method.jboss.JbossJMXOption;

public class JbossPmiMonitorUtil {
	protected static RMIAdaptor server;
	private static final Log logger = LogFactory.getLog(JbossPmiMonitorUtil.class);
	private static long applyMemory = 0;
	private static long totalMemory = 0;
	private static long freeMemory = 0;

	public static PmiInformation getPmiInfo(JbossJMXOption jbossjmxoption) throws IOException, SAXException,
			JbossRemoteException {
		String host = jbossjmxoption.getIpAddr();
		String username = jbossjmxoption.getUsername();
		String password = jbossjmxoption.getPassword();
		int port = jbossjmxoption.getPort();
		if (username == null || username.equals("")) {
			username = "admin";
		}
		if (password == null || password.equals("")) {
			password = "admin";
		}

		Properties pro = new Properties();
		pro.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		pro.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
		pro.setProperty("java.naming.security.principal", username);
		pro.setProperty("java.naming.security.credentials", password);
		pro.setProperty("java.naming.provider.url", "jnp://" + host + ":" + port);

		try {
			InitialContext ic = new InitialContext(pro);
			server = (RMIAdaptor) ic.lookup("jmx/rmi/RMIAdaptor");
			ObjectName name = new ObjectName("jboss.system:type=ServerInfo");
			totalMemory = (Long) server.getAttribute(name, "TotalMemory") / 1024 / 1024;//MB
			freeMemory = (Long) server.getAttribute(name, "FreeMemory") / 1024 / 1024;//MB
			applyMemory = totalMemory - freeMemory;

		} catch (Exception e) {
			logger.error("初始化性能获取上下文失败", e);
		}
		PmiInformation pmiInfo = new PmiInformation();
		pmiInfo.setFreeMemory(freeMemory);
		pmiInfo.setApplyMemory(applyMemory);
		pmiInfo.setTotalMemory(totalMemory);
		return pmiInfo;

	}

	public static PmiInformation getJboss6PmiInfo(JbossJMXOption jbossjmxoption) throws HttpException, IOException {
		String host = jbossjmxoption.getIpAddr();
		String username = jbossjmxoption.getUsername();
		String password = jbossjmxoption.getPassword();
		int port = jbossjmxoption.getPort();
		String url1 = "http://" + host + ":" + port
				+ "/jmx-console/HtmlAdaptor?action=inspectMBean&name=jboss.system%3Atype%3DServerInfo";
		GetMethod method1 = JbossHttpGetUtil.getMethod(url1, username, password);
		String str1 = method1.getResponseBodyAsString();
		method1.releaseConnection();
		Document doc1 = Jsoup.parse(str1);

		Elements formInfos1 = doc1.select("form");
		Element tableInfos1 = formInfos1.get(0);
		Elements trInfo1 = tableInfos1.select("tr");
		trInfo1.remove(0);
		for (Element info : trInfo1) {
			Element param = info.child(0);
			String paramName = param.ownText().trim();
			Element tdInfo = info.child(4);
			Element paramInfo = tdInfo.child(0);
			String paramValue = paramInfo.ownText().trim();
			if ("TotalMemory".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				totalMemory = Long.parseLong(paramValue) / 1024 / 1024;
			} else if ("FreeMemory".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				freeMemory = Long.parseLong(paramValue) / 1024 / 1024;
			}
			applyMemory = totalMemory - freeMemory;
		}
		PmiInformation info = new PmiInformation();
		info.setApplyMemory(applyMemory);
		info.setFreeMemory(freeMemory);
		info.setTotalMemory(totalMemory);
		return info;
	}
}
