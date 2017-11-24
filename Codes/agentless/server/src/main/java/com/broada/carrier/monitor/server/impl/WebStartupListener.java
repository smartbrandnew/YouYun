package com.broada.carrier.monitor.server.impl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.server.impl.config.Config;
import com.broada.component.utils.lang.SimpleProperties;
import com.broada.component.utils.lang.SystemProperties;

/**
 * webapp启动监听
 * @author Jiangjw
 */
public class WebStartupListener implements ServletContextListener {
	private static Logger logger;
	
	/**
	 * 对必要的未设置的系统属性进行设置
	 */
	public static void checkSystemProperties() {
		SystemProperties.setIfNotExists("monitor.logs.dir", Config.getWorkDir() + "/logs");
		SystemProperties.setIfNotExists("monitor.config", Config.getConfDir() + "/config.properties");
		SystemProperties.setIfNotExists("monitor.db.config", Config.getConfDir() + "/jdbc.properties");		
		SystemProperties.setIfNotExists("logback.configurationFile", Config.getConfDir() + "/logback.xml");
		SystemProperties.setIfNotExists("net.sf.ehcache.skipUpdateCheck", "true");		
		
		SimpleProperties props = new SimpleProperties(System.getProperty("monitor.db.config"));
		String type = props.get("database.type");
		String dialect;
		String schema;
		if (type != null && type.equalsIgnoreCase("mysql")) {
			dialect = "org.hibernate.dialect.MySQL5Dialect";
			schema = "";
			type = "MYSQL";
		} else { 
			dialect = "org.hibernate.dialect.Oracle10gDialect";
			schema = props.get("jdbc.username");	// 对于oracle来说，预设置default_schema可以避免用户拥有exp_full_database权限导致的初始化失败
			type = "ORACLE";
		}
		System.setProperty("db.dialect", dialect);
		System.setProperty("db.schema", schema);
		System.setProperty("db.type", type);	
	}
	
	public static void checkSystemProperties(String workDir) {		
	}

	public void contextInitialized(ServletContextEvent event) {
		Config.setWorkDir(event.getServletContext().getRealPath(".."));		
		checkSystemProperties();
		if (logger == null)
			logger = LoggerFactory.getLogger(WebStartupListener.class);
		logger.info("MonitorServer启动...");
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}
}
