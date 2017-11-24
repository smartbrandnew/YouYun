package com.broada.carrier.monitor.impl.mw.jboss.basic;

import java.io.IOException;
import java.util.Properties;

import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jdom.JDOMException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.xml.sax.SAXException;

import com.broada.carrier.monitor.impl.mw.jboss.JbossHttpGetUtil;
import com.broada.carrier.monitor.impl.mw.jboss.JbossRemoteException;
import com.broada.carrier.monitor.method.jboss.JbossJMXOption;

public class JbossBasicMonitorUtil {
	protected static RMIAdaptor server;
	private static final Log logger = LogFactory.getLog(JbossBasicMonitorUtil.class);
	private static String javaVendor = null;
	private static String javaVersion = null;
	private static String oSName = null;
	private static String version = null;
	private static String state = null;

	public static ServerInformation getServerInfo(JbossJMXOption jbossjmxoption) throws IOException, SAXException,
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
			ObjectName name = new ObjectName("jboss.system:type=ServerInfo");
			javaVendor = server.getAttribute(name, "JavaVendor").toString();
			javaVersion = server.getAttribute(name, "JavaVersion").toString();
			oSName = server.getAttribute(name, "OSName").toString();
			ObjectName name1 = new ObjectName("jboss.system:type=Server");
			version = server.getAttribute(name1, "VersionNumber").toString();
			state = server.getAttribute(name1, "Started").toString();
			if ("true".equalsIgnoreCase(state))
				state = "running";
			else
				state = null;
		} catch (Exception e) {
			logger.error("初始化性能获取上下文失败", e);
		}
		ServerInformation serverInfo = new ServerInformation();
		serverInfo.setJavaVendor(javaVendor);
		serverInfo.setJavaVersion(javaVersion);
		serverInfo.setoSName(oSName);
		serverInfo.setState(state);
		serverInfo.setVersion(version);
		return serverInfo;
	}

	public static ServerInformation getJboss6eapServerInfo(JbossJMXOption jbossjmxoption) throws Exception {
		String host = jbossjmxoption.getIpAddr();
		String username = jbossjmxoption.getUsername();
		String password = jbossjmxoption.getPassword();
		int port = jbossjmxoption.getPort();
		String url = "http://" + host + ":" + port + "/management/core-service/platform-mbean/type/runtime";
		GetMethod method = JbossHttpGetUtil.getMethod(url, username, password);
		String str = method.getResponseBodyAsString();
		method.releaseConnection();
		JSONObject json = new JSONObject(str);
		javaVendor = json.get("vm-vendor").toString();
		JSONObject son = json.getJSONObject("system-properties");
		javaVersion = son.get("java.runtime.version").toString();
		oSName = son.get("os.name").toString();
		
		String url2 = "http://" + host + ":" + port + "/management/";
		GetMethod method2 = JbossHttpGetUtil.getMethod(url2, username, password);
		String str2 = method2.getResponseBodyAsString();
		method.releaseConnection();
		JSONObject json2 = new JSONObject(str2);
		version = json2.get("product-version").toString();
		state = "running";

		ServerInformation info = new ServerInformation();
		info.setJavaVendor(javaVendor);
		info.setJavaVersion(javaVersion);
		info.setState(state);
		info.setoSName(oSName);
		info.setVersion(version);
		return info;
	}

	public static ServerInformation getJboss7ServerInfo(JbossJMXOption jbossjmxoption) throws Exception {
		String host = jbossjmxoption.getIpAddr();
		String username = jbossjmxoption.getUsername();
		String password = jbossjmxoption.getPassword();
		int port = jbossjmxoption.getPort();
		String url = "http://" + host + ":" + port + "/management/core-service/platform-mbean/type/runtime";
		GetMethod method = JbossHttpGetUtil.getMethod(url, username, password);
		String str = method.getResponseBodyAsString();
		method.releaseConnection();
		JSONObject json = new JSONObject(str);
		javaVendor = json.get("vm-vendor").toString();
		JSONObject son = json.getJSONObject("system-properties");
		javaVersion = son.get("java.runtime.version").toString();
		oSName = son.get("os.name").toString();

		String url2 = "http://" + host + ":" + port + "/management/";
		GetMethod method2 = JbossHttpGetUtil.getMethod(url2, username, password);
		String str2 = method2.getResponseBodyAsString();
		method.releaseConnection();
		JSONObject json2 = new JSONObject(str2);
		version = json2.get("release-version").toString();
		state = "running";

		ServerInformation info = new ServerInformation();
		info.setJavaVendor(javaVendor);
		info.setJavaVersion(javaVersion);
		info.setState(state);
		info.setoSName(oSName);
		info.setVersion(version);
		return info;
	}

	public static ServerInformation getJboss6ServerInfo(JbossJMXOption jbossjmxoption) throws HttpException, IOException,
			JDOMException {
		String username = jbossjmxoption.getUsername();
		String password = jbossjmxoption.getPassword();
		String host = jbossjmxoption.getIpAddr();
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
			if ("JavaVersion".equalsIgnoreCase(paramName)) {
				javaVersion = paramValue;
			} else if ("JavaVendor".equalsIgnoreCase(paramName)) {
				javaVendor = paramValue;
			} else if ("OSName".equalsIgnoreCase(paramName)) {
				oSName = paramValue;
			}
		}
		String url2 = "http://" + host + ":" + port
				+ "/jmx-console/HtmlAdaptor?action=inspectMBean&name=jboss.system%3Atype%3DServer";
		GetMethod method2 = JbossHttpGetUtil.getMethod(url2, username, password);
		String str2 = method2.getResponseBodyAsString();
		method2.releaseConnection();
		Document doc2 = Jsoup.parse(str2);
		Elements formInfos2 = doc2.select("form");
		Element tableInfo2 = formInfos2.get(0);
		Elements trInfo2 = tableInfo2.select("tr");
		trInfo2.remove(0);
		trInfo2.remove(trInfo2.size() - 1);
		for (Element info : trInfo2) {
			Element param = info.child(0);
			String paramName = param.ownText().trim();
			Element tdInfo = info.child(4);
			Element paramInfo = tdInfo.child(0);
			String paramValue = paramInfo.ownText().trim();
			if ("VersionNumber".equalsIgnoreCase(paramName))
				version = paramValue;
		}
		ServerInformation info = new ServerInformation();
		info.setJavaVendor(javaVendor);
		info.setJavaVersion(javaVersion);
		info.setoSName(oSName);
		info.setState("running");
		info.setVersion(version);
		return info;
	}
}
