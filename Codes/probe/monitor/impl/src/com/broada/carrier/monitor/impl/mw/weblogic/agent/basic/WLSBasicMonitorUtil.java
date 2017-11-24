package com.broada.carrier.monitor.impl.mw.weblogic.agent.basic;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.xml.sax.SAXException;

import com.broada.carrier.monitor.impl.mw.weblogic.agent.WLSRemoteException;
import com.broada.carrier.monitor.method.weblogic.agent.WebLogicJMXOption;
import com.broada.utils.Base64Util;
import com.broada.utils.StringUtil;

public class WLSBasicMonitorUtil {
	private static final String INFO_DIGESTER_RULE = "conf/wls/info-digester-rule.xml";
	public static final String WLSBASE_REQUEST_PATH = "/WebLogicMonitorServlet?type=info";
	public static final String WLSEJB_REQUEST_PATH = "/WebLogicMonitorServlet?type=ejb";
	public static final String WLS_WEBAPP_REQUEST_PATH = "/WebLogicMonitorServlet?type=webapp";
	public static final String WLS_SERVLET_REQUEST_PATH = "/WebLogicMonitorServlet?type=servlet";
	public static final String WLS_CLUSTER_REQUEST_PATH = "/WebLogicMonitorServlet?type=servers";
	public static final String WLS_JDBC_REQUEST_PATH = "/WebLogicMonitorServlet?type=jdbcconnection";
	public static final String WLS_THREAD_REQUEST_PATH = "/WebLogicMonitorServlet?type=thread";
	public static final String WLS_SUBSYSTEM_REQUEST_PATH = "/WebLogicMonitorServlet?type=subsystem";

	public static ServerInformation getServerInformation(String _url) throws MalformedURLException, IOException,
			SAXException, WLSRemoteException {
		if (StringUtil.isNullOrBlank(_url)) {
			throw new MalformedURLException("URL不能为空.");
		}
		Digester digester = DigesterLoader.createDigester(new File(INFO_DIGESTER_RULE).toURI().toURL());
		digester.setValidating(false);
		URL url = new URL(_url);
		ServerInformation serverInformation = (ServerInformation) digester.parse(url.openStream());
		if (!StringUtil.isNullOrBlank(serverInformation.getMessage())
				|| !StringUtil.isNullOrBlank(serverInformation.getDetail())) {
			throw new WLSRemoteException(serverInformation.getMessage(), serverInformation.getDetail());
		}
		return serverInformation;
	}

	public static String encode(String username, String password) throws UnsupportedEncodingException {
		String all = "username=" + username + "&password=" + password;

		all = Base64Util.encode(all);

		return URLEncoder.encode(all, "8859_1");
	}

	public static String decode(String all) throws UnsupportedEncodingException {
		return Base64Util.decode(all);
	}

	public static String getUrl(String ipAddress, int port, String path, String username, String password)
			throws UnsupportedEncodingException {
		StringBuffer buffer = new StringBuffer("http://");
		buffer.append(ipAddress).append(":").append(port).append(path).append("&all=")
				.append(encode(username, password));
		return buffer.toString();
	}

	public static String getUrl(String ipAddress, int port, String agentName, String path, String username,
			String password) throws UnsupportedEncodingException {
		StringBuffer buffer = new StringBuffer("http://");
		if (username == null || password == null || "".equals(username.trim()) || "".equals(password.trim())) {
			buffer.append(ipAddress).append(":").append(port).append("/").append(agentName).append(path);
		} else {
			buffer.append(ipAddress).append(":").append(port).append("/").append(agentName).append(path)
					.append("&all=").append(encode(username, password));
		}
		return buffer.toString();
	}

	public static String getBaseInfoUrl(WebLogicJMXOption webLogicJMXOption) throws UnsupportedEncodingException {
		return getUrl(webLogicJMXOption.getHost(), webLogicJMXOption.isIfCluster() ? webLogicJMXOption.getProxyPort()
				: webLogicJMXOption.getPort(), webLogicJMXOption.getAgentName(),
				WLSBasicMonitorUtil.WLSBASE_REQUEST_PATH, webLogicJMXOption.getUsername(),
				webLogicJMXOption.getPassword());
	}

	public static String getEJBInfoUrl(WebLogicJMXOption webLogicJMXOption) throws UnsupportedEncodingException {
		return getUrl(webLogicJMXOption.getHost(), webLogicJMXOption.isIfCluster() ? webLogicJMXOption.getProxyPort()
				: webLogicJMXOption.getPort(), webLogicJMXOption.getAgentName(),
				WLSBasicMonitorUtil.WLSEJB_REQUEST_PATH, webLogicJMXOption.getUsername(),
				webLogicJMXOption.getPassword());
	}

	public static String getWebAppInfoUrl(WebLogicJMXOption webLogicJMXOption) throws UnsupportedEncodingException {
		return getUrl(webLogicJMXOption.getHost(), webLogicJMXOption.isIfCluster() ? webLogicJMXOption.getProxyPort()
				: webLogicJMXOption.getPort(), webLogicJMXOption.getAgentName(),
				WLSBasicMonitorUtil.WLS_WEBAPP_REQUEST_PATH, webLogicJMXOption.getUsername(),
				webLogicJMXOption.getPassword());
	}

	public static String getServletInfoUrl(WebLogicJMXOption webLogicJMXOption) throws UnsupportedEncodingException {
		return getUrl(webLogicJMXOption.getHost(), webLogicJMXOption.isIfCluster() ? webLogicJMXOption.getProxyPort()
				: webLogicJMXOption.getPort(), webLogicJMXOption.getAgentName(),
				WLSBasicMonitorUtil.WLS_SERVLET_REQUEST_PATH, webLogicJMXOption.getUsername(),
				webLogicJMXOption.getPassword());
	}

	public static String getClusterInfoUrl(WebLogicJMXOption webLogicJMXOption) throws UnsupportedEncodingException {
		return getUrl(webLogicJMXOption.getHost(), webLogicJMXOption.isIfCluster() ? webLogicJMXOption.getProxyPort()
				: webLogicJMXOption.getPort(), webLogicJMXOption.getAgentName(),
				WLSBasicMonitorUtil.WLS_CLUSTER_REQUEST_PATH, webLogicJMXOption.getUsername(),
				webLogicJMXOption.getPassword());
	}

	public static String getJdbcInfoUrl(WebLogicJMXOption webLogicJMXOption) throws UnsupportedEncodingException {
		return getUrl(webLogicJMXOption.getHost(), webLogicJMXOption.isIfCluster() ? webLogicJMXOption.getProxyPort()
				: webLogicJMXOption.getPort(), webLogicJMXOption.getAgentName(),
				WLSBasicMonitorUtil.WLS_JDBC_REQUEST_PATH, webLogicJMXOption.getUsername(),
				webLogicJMXOption.getPassword());
	}

	public static String getThreadInfoUrl(WebLogicJMXOption webLogicJMXOption) throws UnsupportedEncodingException {
		return getUrl(webLogicJMXOption.getHost(), webLogicJMXOption.isIfCluster() ? webLogicJMXOption.getProxyPort()
				: webLogicJMXOption.getPort(), webLogicJMXOption.getAgentName(),
				WLSBasicMonitorUtil.WLS_THREAD_REQUEST_PATH, webLogicJMXOption.getUsername(),
				webLogicJMXOption.getPassword());
	}

	public static String getSubSystemInfoUrl(WebLogicJMXOption webLogicJMXOption) throws UnsupportedEncodingException {
		return getUrl(webLogicJMXOption.getHost(), webLogicJMXOption.isIfCluster() ? webLogicJMXOption.getProxyPort()
				: webLogicJMXOption.getPort(), webLogicJMXOption.getAgentName(),
				WLSBasicMonitorUtil.WLS_SUBSYSTEM_REQUEST_PATH, webLogicJMXOption.getUsername(),
				webLogicJMXOption.getPassword());
	}
}
