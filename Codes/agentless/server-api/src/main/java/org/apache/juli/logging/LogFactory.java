package org.apache.juli.logging;

import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.common.tomcat.Slf4jLogger;

public class LogFactory {
	public static Log getLog(Class<?> clazz) throws LogConfigurationException {
		return new Slf4jLogger(LoggerFactory.getLogger(clazz));
	}

	public static Log getLog(String name)	throws LogConfigurationException {
		return new Slf4jLogger(LoggerFactory.getLogger(name));
	}
	
  public static void release(ClassLoader classLoader) {  	
  }
  
  public static void releaseAll() {  	
  }
}
