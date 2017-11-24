package com.broada.agent.config;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import com.broada.numen.agent.api.entity.AgentInitParams;
import com.broada.numen.agent.manage.service.ManageAgent;
import com.broada.numen.agent.original.service.OriginalAgent;
import com.broada.numen.agent.script.service.ExecuteException;
import com.broada.numen.agent.script.service.ExecuteService;

/**
 * 代理端执行脚本
 * 
 * @author
 *
 */
public class HostAgentClient {
  private static final Log logger = LogFactory.getLog(HostAgentClient.class);
  private static Map<String, Object> agents = new ConcurrentHashMap<String, Object>();
  private static String autosyncServerUrl = "http://%s:8989/numen/autosync/";
  
  /**
   * 设置传递给agent的远程同步地址
   * @param autosyncServerUrl
   */
  public static void setAutosyncServerUrl(String autosyncServerUrl) {
		HostAgentClient.autosyncServerUrl = autosyncServerUrl;
	}

	public static OriginalAgent getHostAgent(String host) throws RemoteException {
    return getHostAgent(host, 1850, "uniagent");
  }
  
  public static OriginalAgent getHostAgent(String host, int port) throws RemoteException {
    return getHostAgent(host, port, "uniagent");
  }  

  public static OriginalAgent getHostAgent(String host, int port, String rmiName) throws RemoteException {
    getManageAgent(host, port);
    String key = getKey(host, port, rmiName);
    OriginalAgent agent = (OriginalAgent) agents.get(key);
    if (agent != null) {
      try {
        agent.ping();
        return agent;
      } catch (RemoteException e) {
        logger.info("缓存里的Agent已经失效,重新连接,KEY=" + key, e);
      }
      removeAgent(host, port, rmiName);
    }
    String serviceUrl = "rmi://" + host + ":" + port + "/" + rmiName;
    RmiProxyFactoryBean rmiProxyFactoryBean = new RmiProxyFactoryBean();
    rmiProxyFactoryBean.setServiceInterface(OriginalAgent.class);
    rmiProxyFactoryBean.setServiceUrl(serviceUrl);
    rmiProxyFactoryBean.setLookupStubOnStartup(true);
    rmiProxyFactoryBean.setRefreshStubOnConnectFailure(true);
    rmiProxyFactoryBean.afterPropertiesSet();
    agent = (OriginalAgent) rmiProxyFactoryBean.getObject();
    agents.put(key, agent);       
    return agent;
  }

  /**
   * 获取升级代理对象
   * 
   * @param host
   * @param port
   * @return
   * @throws RemoteException
   */
  public static ManageAgent getManageAgent(String host, int port) throws RemoteException {
    String key = getKey(host, port, "manage");
    com.broada.numen.agent.manage.service.ManageAgent agent = (ManageAgent) agents.get(key);
    if (agent != null) {
      try {
        agent.ping();
        return agent;
      } catch (RemoteException e) {
        logger.info("缓存里的Agent已经失效,重新连接,KEY=" + key, e);
      }
      removeAgent(host, port, "manage");
    }
    String serviceUrl = "rmi://" + host + ":" + port + "/manage";
    RmiProxyFactoryBean rmiProxyFactoryBean = new RmiProxyFactoryBean();
    rmiProxyFactoryBean.setServiceInterface(ManageAgent.class);
    rmiProxyFactoryBean.setServiceUrl(serviceUrl);
    rmiProxyFactoryBean.setLookupStubOnStartup(true);
    rmiProxyFactoryBean.setRefreshStubOnConnectFailure(true);
    rmiProxyFactoryBean.afterPropertiesSet();
    agent = (ManageAgent) rmiProxyFactoryBean.getObject();    
    try {
    	agent.init(new AgentInitParams(autosyncServerUrl));
    } catch (Throwable e) {
    	logger.debug("Agent不支持init方法，无法实现自动升级：" + host + ":" + port, e);
    }
    agents.put(key, agent);
    return agent;
  }

  /**
   * 获取脚本执行服务对象
   * 
   * @param host
   * @param port
   * @param rmiName
   * @return
   * @throws RemoteException
   */
  public static ExecuteService getCommonCLIAgent(String host, int port, String rmiName) throws RemoteException {
    getManageAgent(host, port);
    String key = getKey(host, port, rmiName);
    ExecuteService agent = (ExecuteService) agents.get(key);
    if (agent != null) {
      try {
        agent.ping();
        return agent;
      } catch (RemoteException e) {
        logger.info("缓存里的Agent已经失效,重新连接,KEY=" + key, e);
      }
      removeAgent(host, port, rmiName);
    }
    String serviceUrl = "rmi://" + host + ":" + port + "/" + rmiName;
    RmiProxyFactoryBean rmiProxyFactoryBean = new RmiProxyFactoryBean();
    rmiProxyFactoryBean.setServiceInterface(ExecuteService.class);
    rmiProxyFactoryBean.setServiceUrl(serviceUrl);
    rmiProxyFactoryBean.setLookupStubOnStartup(true);
    rmiProxyFactoryBean.setRefreshStubOnConnectFailure(true);
    rmiProxyFactoryBean.afterPropertiesSet();
    agent = (ExecuteService) rmiProxyFactoryBean.getObject();
    agents.put(key, agent);
    return agent;
  }

  private static String getKey(String host, int port, String rmiName) {
    return host + ":" + port + ":" + rmiName;
  }

  /**
   * 去除某个主机上Agent的缓存
   * @param host
   * @param port
   * @param rmiName
   */
  public static void removeAgent(String host, int port, String rmiName) {
    agents.remove(getKey(host, port, rmiName));
  }
}
