package com.broada.carrier.monitor.method.tongweb;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class TongWebMonitorMethodOption extends MonitorMethod { 
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolTongWeb";
	
  public TongWebMonitorMethodOption() {
		super();
	}

	public TongWebMonitorMethodOption(MonitorMethod copy) {
		super(copy);
	}

	public int getPort() {
    return getProperties().getByMethod(1099);
  }

  public void setPort(int port) {
    getProperties().setByMethod(port);
  }

  public String getJndiName() {
  	return getProperties().getByMethod("RMIConnector_teas");
  }

  public void setJndiName(String name) {
  	getProperties().setByMethod(name);
  }

}
