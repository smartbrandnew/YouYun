package com.broada.carrier.monitor.probe.impl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.probe.impl.config.Config;
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
		SystemProperties.setIfNotExists("monitor.conf.dir", Config.getConfDir());
		SystemProperties.setIfNotExists("logback.configurationFile", Config.getConfDir() + "/logback.xml");
		SystemProperties.setIfNotExists("net.sf.ehcache.skipUpdateCheck", "true");		
		SystemProperties.setIfNotExists("java.library.path", Config.getWorkDir() + "/bin");
	}
	
	public static void checkSystemProperties(String workDir) {		
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		Config.setWorkDir(event.getServletContext().getRealPath(".."));		
		checkSystemProperties();
		if (logger == null)
			logger = LoggerFactory.getLogger(WebStartupListener.class);
		logger.info("MonitorProbe启动...");
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}
}
