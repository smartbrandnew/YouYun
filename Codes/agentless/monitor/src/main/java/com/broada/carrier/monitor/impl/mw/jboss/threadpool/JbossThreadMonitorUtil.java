package com.broada.carrier.monitor.impl.mw.jboss.threadpool;

import java.io.IOException;
import java.util.Properties;

import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.xml.sax.SAXException;

import com.broada.carrier.monitor.impl.mw.jboss.JbossRemoteException;
import com.broada.carrier.monitor.method.jboss.JbossJMXOption;

public class JbossThreadMonitorUtil {
	protected static RMIAdaptor server;
	private static final Log logger = LogFactory.getLog(JbossThreadMonitorUtil.class);

	public static ThreadInformation getThreadInfo(JbossJMXOption jbossjmxoption) throws IOException, SAXException,
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

		int queueSize = 0;
		int maximumQueueSize = 0;
		int minimumPoolSize = 0;
		int maximumPoolSize = 0;
		int poolNumber = 0;
		int activeThreads = 0;

		try {
			InitialContext ic = new InitialContext(pro);
			server = (RMIAdaptor) ic.lookup("jmx/rmi/RMIAdaptor");
			ObjectName name = new ObjectName("jboss.system:service=ThreadPool");
			queueSize = (Integer) server.getAttribute(name, "QueueSize");//个
			maximumQueueSize = (Integer) server.getAttribute(name, "MaximumQueueSize");
			minimumPoolSize = (Integer) server.getAttribute(name, "MinimumPoolSize");
			maximumPoolSize = (Integer) server.getAttribute(name, "MaximumPoolSize");
			poolNumber = (Integer) server.getAttribute(name, "MaximumPoolSize");
			//activeThreads = (Integer) server.getAttribute(name, "PoolNumber");
		} catch (Exception e) {
			logger.error("初始化性能获取上下文失败", e);
		}
		
		Properties pro2 = new Properties();
		pro2.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		pro2.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
		pro2.setProperty("java.naming.security.principal", username);
		pro2.setProperty("java.naming.security.credentials", password);
		pro2.setProperty("java.naming.provider.url", "jnp://" + host + ":" + port);

		try {
			InitialContext ic = new InitialContext(pro);
			server = (RMIAdaptor) ic.lookup("jmx/rmi/RMIAdaptor");
			ObjectName name = new ObjectName("jboss.system:type=ServerInfo");
			activeThreads = (Integer) server.getAttribute(name, "ActiveThreadCount");
		} catch (Exception e) {
			logger.error("初始化性能获取上下文失败", e);
		}
		
		String name = "default";
		ThreadInformation threadInfo = new ThreadInformation();
		threadInfo.setMaximumPoolSize(maximumPoolSize);
		threadInfo.setMaximumQueueSize(maximumQueueSize);
		threadInfo.setMinimumPoolSize(minimumPoolSize);
		threadInfo.setPoolNumber(poolNumber);
		threadInfo.setQueueSize(queueSize);
		threadInfo.setName(name);
		threadInfo.setActiveThreads(activeThreads);
		return threadInfo;
	}
}
