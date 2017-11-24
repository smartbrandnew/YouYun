package com.broada.carrier.monitor.impl.mw.jboss.webapp;

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

public class JbossWebMonitorUtil {
	protected static RMIAdaptor server;
	private static final Log logger = LogFactory.getLog(JbossWebMonitorUtil.class);
	private static int sessionMaxAliveTime = 0;
	private static int activeSessions = 0;
	private static int sessionAverageAliveTime = 0;
	private static int maxActive = 0;
	private static int expiredSessions = 0;
	private static int rejectedSessions = 0;
	private static int sessionCounter = 0;

	public static WebInformation getWebInfo(JbossJMXOption jbossjmxoption) throws IOException, SAXException,
			JbossRemoteException {
		String host = jbossjmxoption.getIpAddr();
		String username = jbossjmxoption.getUsername();
		String passd = jbossjmxoption.getPassword();
		int port = jbossjmxoption.getPort();
		if (username == null || username.equals("")) {
			username = "admin";
		}
		if (passd == null || passd.equals("")) {
			passd = "admin";
		}
		Properties pro = new Properties();
		pro.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		pro.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
		pro.setProperty("java.naming.security.principal", username);
		pro.setProperty("java.naming.security.credentials", passd);
		pro.setProperty("java.naming.provider.url", "jnp://" + host + ":" + port);

		try {
			InitialContext ic = new InitialContext(pro);
			server = (RMIAdaptor) ic.lookup("jmx/rmi/RMIAdaptor");
			ObjectName name = new ObjectName("jboss.web:host=localhost,path=/web-console,type=Manager");
			sessionMaxAliveTime = (Integer) server.getAttribute(name, "sessionMaxAliveTime");
			activeSessions = (Integer) server.getAttribute(name, "activeSessions");
			sessionAverageAliveTime = (Integer) server.getAttribute(name, "sessionAverageAliveTime");
			maxActive = (Integer) server.getAttribute(name, "maxActive");
			expiredSessions = (Integer) server.getAttribute(name, "expiredSessions");
			rejectedSessions = (Integer) server.getAttribute(name, "rejectedSessions");
			sessionCounter = (Integer) server.getAttribute(name, "sessionCounter");
		} catch (Exception e) {
			logger.error("初始化性能获取上下文失败", e);
		}
		String name = "manager";
		WebInformation webInfo = new WebInformation();
		webInfo.setActiveSessions(activeSessions);
		webInfo.setExpiredSessions(expiredSessions);
		webInfo.setMaxActive(maxActive);
		webInfo.setRejectedSessions(rejectedSessions);
		webInfo.setSessionAverageAliveTime(sessionAverageAliveTime);
		webInfo.setSessionCounter(sessionCounter);
		webInfo.setSessionMaxAliveTime(sessionMaxAliveTime);
		webInfo.setName(name);
		return webInfo;
	}

	public static WebInformation getJboss6WebInfo(JbossJMXOption jbossjmxoption) throws HttpException, IOException {
		String host = jbossjmxoption.getIpAddr();
		String username = jbossjmxoption.getUsername();
		String password = jbossjmxoption.getPassword();
		int port = jbossjmxoption.getPort();
		String url = "http://"
				+ host
				+ ":"
				+ port
				+ "/jmx-console/HtmlAdaptor?action=inspectMBean&name=jboss.web%3Atype%3DManager%2Cpath%3D%2Fjmx-console%2Chost%3Dlocalhost";
		GetMethod method = JbossHttpGetUtil.getMethod(url, username, password);
		String str = method.getResponseBodyAsString();
		method.releaseConnection();
		Document doc = Jsoup.parse(str);

		Elements formInfos = doc.select("form");
		Element tableInfos = formInfos.get(0);
		Elements trInfo = tableInfos.select("tr");
		trInfo.remove(0);
		trInfo.remove(trInfo.size() - 1);
		for (Element info : trInfo) {
			Element param = info.child(0);
			String paramName = param.ownText().trim();
			Element tdInfo = info.child(4);
			Element paramInfo = tdInfo.child(0);
			String paramValue = paramInfo.ownText().trim();
			if (paramInfo.getAllElements().size() != 1)
				paramValue = paramInfo.child(0).val().trim();
			if ("sessionMaxAliveTime".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				sessionMaxAliveTime = Integer.parseInt(paramValue);
			} else if ("activeSessions".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				activeSessions = Integer.parseInt(paramValue);
			} else if ("sessionCounter".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				sessionCounter = Integer.parseInt(paramValue);
			} else if ("sessionAverageAliveTime".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				sessionAverageAliveTime = Integer.parseInt(paramValue);
			} else if ("maxActive".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				maxActive = Integer.parseInt(paramValue);
			} else if ("expiredSessions".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				expiredSessions = Integer.parseInt(paramValue);
			} else if ("rejectedSessions".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				rejectedSessions = Integer.parseInt(paramValue);
			}
		}
		String name = "manager";
		WebInformation info = new WebInformation();
		info.setActiveSessions(activeSessions);
		info.setExpiredSessions(expiredSessions);
		info.setMaxActive(maxActive);
		info.setRejectedSessions(rejectedSessions);
		info.setSessionAverageAliveTime(sessionAverageAliveTime);
		info.setSessionCounter(sessionCounter);
		info.setSessionMaxAliveTime(sessionMaxAliveTime);
		info.setName(name);
		return info;
	}
}
