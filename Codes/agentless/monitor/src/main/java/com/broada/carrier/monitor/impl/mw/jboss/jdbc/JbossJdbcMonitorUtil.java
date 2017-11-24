package com.broada.carrier.monitor.impl.mw.jboss.jdbc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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

public class JbossJdbcMonitorUtil {
	private static final Log logger = LogFactory.getLog(JbossJdbcMonitorUtil.class);
	protected static RMIAdaptor server;
	private static long availableConnCount = 0;
	private static long inUseConnCount = 0;
	private static int connCreatedCount = 0;
	private static int connDestroyedCount = 0;
	private static long maxConnInUseCount = 0;
	private static int connCount = 0;
	private static int blockingTimeoutMillis = 0;
	private static int maxSize = 0;
	private static int minSize = 0;
	private static long idleTimeoutMinutes = 0;
	private static double usePercent = 0;

	public static List<JdbcInformation> getJdbcInfo(JbossJMXOption jbossjmxoption) throws IOException, SAXException,
			JbossRemoteException {
		List<JdbcInformation> jdbcInfoList = new ArrayList<JdbcInformation>();
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
			Set<ObjectName> set=server.queryNames(new ObjectName("jboss.jca:service=ManagedConnectionPool,*"),null);
			for(ObjectName name:set){
				availableConnCount = (Long) server.getAttribute(name, "AvailableConnectionCount");
				inUseConnCount = (Long) server.getAttribute(name, "InUseConnectionCount");
				connCreatedCount = (Integer) server.getAttribute(name, "ConnectionCreatedCount");
				connDestroyedCount = (Integer) server.getAttribute(name, "ConnectionDestroyedCount");
				maxConnInUseCount = (Long) server.getAttribute(name, "MaxConnectionsInUseCount");
				connCount = (Integer) server.getAttribute(name, "ConnectionCount");
				blockingTimeoutMillis = (Integer) server.getAttribute(name, "BlockingTimeoutMillis") / 1000;
				maxSize = (Integer) server.getAttribute(name, "MaxSize");
				minSize = (Integer) server.getAttribute(name, "MinSize");
				idleTimeoutMinutes = (Long) server.getAttribute(name, "IdleTimeoutMinutes");
				java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
				usePercent = (double) (100 * (inUseConnCount + 0.0) / availableConnCount);
				usePercent = Double.parseDouble(df.format(usePercent));
				JdbcInformation jdbcInfo = new JdbcInformation();
				jdbcInfo.setName(name.toString().split(",")[1].split("=")[1]);
				jdbcInfo.setAvailableConnCount(availableConnCount);
				jdbcInfo.setBlockingTimeoutMillis(blockingTimeoutMillis);
				jdbcInfo.setConnCount(connCount);
				jdbcInfo.setConnCreatedCount(connCreatedCount);
				jdbcInfo.setConnDestroyedCount(connDestroyedCount);
				jdbcInfo.setIdleTimeoutMinutes(idleTimeoutMinutes);
				jdbcInfo.setInUseConnCount(inUseConnCount);
				jdbcInfo.setMaxConnInUseCount(maxConnInUseCount);
				jdbcInfo.setMaxSize(maxSize);
				jdbcInfo.setMinSize(minSize);
				jdbcInfo.setUsePercent(usePercent);
				jdbcInfoList.add(jdbcInfo);
				
			}
		} catch (Exception e) {
			logger.error("初始化性能获取上下文失败", e);
		}
		
		return jdbcInfoList;

	}

	public static List<JdbcInformation> getJboss6JdbcInfo(JbossJMXOption jbossjmxoption) throws HttpException, IOException {
		List<JdbcInformation> jdbcInfoList = new ArrayList<JdbcInformation>();
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
		String url = "http://"
				+ host
				+ ":"
				+ port
				+ "/jmx-console/HtmlAdaptor?action=inspectMBean&name=jboss.jca%3Aservice%3DManagedConnectionPool%2Cname%3DDefaultDS";
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
			if ("MaxConnectionsInUseCount".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				maxConnInUseCount = Long.parseLong(paramValue);
			} else if ("ConnectionDestroyedCount".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				connDestroyedCount = Integer.parseInt(paramValue);
			} else if ("MaxSize".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				maxSize = Integer.parseInt(paramValue);
			} else if ("ConnectionCount".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				connCount = Integer.parseInt(paramValue);
			} else if ("BlockingTimeoutMillis".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				blockingTimeoutMillis = Integer.parseInt(paramValue) / 1000;
			} else if ("AvailableConnectionCount".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				availableConnCount = Long.parseLong(paramValue);
			} else if ("ConnectionCreatedCount".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				connCreatedCount = Integer.parseInt(paramValue);
			} else if ("IdleTimeoutMinutes".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				idleTimeoutMinutes = Long.parseLong(paramValue);
			} else if ("MinSize".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				minSize = Integer.parseInt(paramValue);
			} else if ("InUseConnectionCount".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				inUseConnCount = Long.parseLong(paramValue);
			}
		}
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
		usePercent = (double) (100 * (inUseConnCount + 0.0) / availableConnCount);
		usePercent = Double.parseDouble(df.format(usePercent));

		JdbcInformation info = new JdbcInformation();
		info.setName("DefaultDS");
		info.setAvailableConnCount(availableConnCount);
		info.setBlockingTimeoutMillis(blockingTimeoutMillis);
		info.setConnCount(connCount);
		info.setConnCreatedCount(connCreatedCount);
		info.setConnDestroyedCount(connDestroyedCount);
		info.setIdleTimeoutMinutes(idleTimeoutMinutes);
		info.setInUseConnCount(inUseConnCount);
		info.setMaxConnInUseCount(maxConnInUseCount);
		info.setMaxSize(maxSize);
		info.setMinSize(minSize);
		info.setUsePercent(usePercent);
		jdbcInfoList.add(info);
		return jdbcInfoList;
	}
}
