package com.broada.carrier.monitor.server.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
import com.broada.carrier.monitor.server.api.client.restful.RestfulServerServiceFactory;
import com.broada.carrier.monitor.server.impl.db.DataSource;
import com.broada.component.utils.runcheck.DataSourceInfoProvider;
import com.broada.component.utils.runcheck.RuntimeChecker;

public class TestRuntime {
	private static Logger logger;	
	private static ApplicationContext context;
	private static ServerServiceFactory serviceFactory;		

	/**
	 * 获取默认实例
	 * @return
	 */
	public static ServerServiceFactory getServiceFactory() {
		if (serviceFactory == null) {
			synchronized (TestRuntime.class) {
				if (serviceFactory == null) {
					//if (System.getProperty("monitor.test.server", "false").equals("true"))
						serviceFactory = new RestfulServerServiceFactory("localhost", 9140);
					//else						
					//	serviceFactory = new TestServiceFactory();
				}
			}
		}
		return serviceFactory;
	}
	
	public static ApplicationContext getContext() {
		if (context == null) {
			synchronized (TestRuntime.class) {
				if (context == null) {				
					if (logger == null)
						logger = LoggerFactory.getLogger(TestRuntime.class);
							
					try {
						context = new FileSystemXmlApplicationContext(new String[] { "webapp/WEB-INF/spring-base.xml", "webapp/WEB-INF/spring-logic.xml" });
						DataSourceInfoProvider.addDataSource(checkBean(DataSource.class).getDataSource());
						RuntimeChecker.getDefault().startup();			
					} catch (Throwable e) {		
						logger.error("启动失败。错误：", e);
						System.exit(1);					
					}
				}
			}
		}
		return context;
	}
	
	static {
		WebStartupListener.checkSystemProperties();
	}

	/**
	 * 获取spring环境下的bean对象
	 * @param cls
	 * @return
	 */
	public static <T> T checkBean(Class<T> cls) {
		return getContext().getBean(cls);
	}	
}
