package com.broada.carrier.monitor.impl.mw.tongweb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.remote.rmi.RMIConnector;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Properties;


public class RMIConnectorFactory {
  private static final Log logger = LogFactory.getLog(RMIConnectorFactory.class);
  private static HashMap connectors = new HashMap();

  public static RMIConnector getRMIConnector(String ipAddr, String name, int port) throws NamingException {
    String key = ipAddr + name + port;
    RMIConnector connector = null;

    synchronized (connectors) {
      connector = (RMIConnector) connectors.get(key);
      if (connector != null) {
        if(logger.isInfoEnabled())
          logger.info("Cache hit!");
        return connector;
      }

      Properties properties = new Properties();
      properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.tongweb.naming.interfaces.NamingContextFactory");
      //JNDI端口号
      properties.setProperty(Context.PROVIDER_URL, ipAddr + ":" + port);
      Context context = new InitialContext(properties);
      //JNDI名字
      connector =  (RMIConnector) context.lookup(name);
      
      connectors.put(key, connector);
      if(logger.isInfoEnabled())
        logger.info("Cache don't hit!");
    }
    
    return connector;

  }
  
  public static void removeAll(){
    synchronized (connectors) {
      connectors.clear();
    }
  }
  
  public static void remove(Object key){
    synchronized (connectors) {
      connectors.remove(key);
    }
  }
}
