package com.broada.carrier.monitor.method.resin;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

/**
 * ResinJMX配置参数
 * @author 杨帆
 * 
 */
public class ResinJMXOption extends MonitorMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolResinAgent";
	
  public ResinJMXOption() {
		super();
	}

	public ResinJMXOption(MonitorMethod copy) {
		super(copy);
	}

	public int getPort() {
  	return getProperties().getByMethod(8080);
  }

  public void setPort(int port) {
    getProperties().setByMethod(port);
  }

  public String getAgentName() {
  	return getProperties().getByMethod("resinAgent");
  }

  public void setAgentName(String agentName) {
  	getProperties().setByMethod(agentName);
  }

  public String getWebAppRoot() {
  	return getProperties().getByMethod("localhost:" + getPort());
  }

  public void setWebAppRoot(String webAppRoot) {
  	getProperties().setByMethod(webAppRoot);
  }

}
