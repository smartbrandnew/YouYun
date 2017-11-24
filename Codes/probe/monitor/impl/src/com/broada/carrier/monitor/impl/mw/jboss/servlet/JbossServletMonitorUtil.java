package com.broada.carrier.monitor.impl.mw.jboss.servlet;

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

public class JbossServletMonitorUtil {
	protected static RMIAdaptor server;
	private static final Log logger = LogFactory.getLog(JbossServletMonitorUtil.class);
	private static long maxTime = 0;
	private static long processingTime = 0;
	private static int requestCount = 0;
	private static int errorCount = 0;

	public static ServletInformation getServletInfo(JbossJMXOption jbossjmxoption) throws IOException, SAXException,
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
			ObjectName name = new ObjectName(
					"jboss.web:J2EEApplication=none,J2EEServer=none,WebModule=//localhost/,j2eeType=Servlet,name=default");
			maxTime = (Long) server.getAttribute(name, "maxTime");//毫秒
			logger.info("最大处理时间："+maxTime);
			processingTime = (Long) server.getAttribute(name, "processingTime");//毫秒
			logger.info("平均处理时间："+processingTime);
			requestCount = (Integer) server.getAttribute(name, "requestCount");//个
			logger.info("请求总数："+requestCount);
			errorCount = (Integer) server.getAttribute(name, "errorCount");
			logger.info("错误总数："+errorCount);
		} catch (Exception e) {
			logger.error("初始化性能获取上下文失败", e);
		}
		String name = "default";
		ServletInformation result = new ServletInformation();
		result.setMaxTime(maxTime);
		result.setProcessingTime(processingTime);
		result.setRequestCount(requestCount);
		result.setName(name);
		return result;
	}

	public static ServletInformation getJboss6ServletInfo(JbossJMXOption jbossjmxoption) throws HttpException,
			IOException {
		String host = jbossjmxoption.getIpAddr();
		String username = jbossjmxoption.getUsername();
		String password = jbossjmxoption.getPassword();
		int port = jbossjmxoption.getPort();
		String url = "http://"
				+ host
				+ ":"
				+ port
				+ "/jmx-console/HtmlAdaptor?action=inspectMBean&name=jboss.web%3Aj2eeType%3DServlet%2Cname%3Ddefault%2CWebModule%3D%2F%2Flocalhost%2Fjmx-console%2CJ2EEApplication%3Dnone%2CJ2EEServer%3Dnone";
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
			if ("maxTime".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				maxTime = Long.parseLong(paramValue);
			} else if ("processingTime".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				processingTime = Long.parseLong(paramValue);
			} else if ("requestCount".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				requestCount = Integer.parseInt(paramValue);
			} else if ("errorCount".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				errorCount = Integer.parseInt(paramValue);
			}
		}
		String name = "default";
		ServletInformation result = new ServletInformation();
		result.setMaxTime(maxTime);
		result.setProcessingTime(processingTime);
		result.setRequestCount(requestCount);
		result.setErrorCount(errorCount);
		result.setName(name);
		return result;
	}
}
