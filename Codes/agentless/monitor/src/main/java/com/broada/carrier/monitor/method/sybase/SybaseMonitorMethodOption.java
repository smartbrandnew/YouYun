package com.broada.carrier.monitor.method.sybase;

import com.broada.carrier.monitor.method.common.JdbcMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;


/**
 * 
 * SYBASE参数
 * 
 * @author Wangx (wangx@broada.com)
 * Create By 2008-5-27 上午11:35:04
 */

public class SybaseMonitorMethodOption extends JdbcMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolSybase";
	
  public SybaseMonitorMethodOption() {
		super();
	}

	public SybaseMonitorMethodOption(MonitorMethod copy) {
		super(copy);
	}

	public int getPort() {
    return getProperties().getByMethod(5000);
  }

  public void setPort(int port) {
    getProperties().setByMethod(port);
  }
}
