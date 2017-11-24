package com.broada.carrier.monitor.method.mysql;

import com.broada.carrier.monitor.method.common.JdbcMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

/**
 * @author lixy (lixy@broada.com.cn) Create By 2008-5-30 下午02:11:00
 */
public class MySQLMonitorMethodOption extends JdbcMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolMysql";
	
  public MySQLMonitorMethodOption() {
		super();
	}

	public MySQLMonitorMethodOption(MonitorMethod copy) {
		super(copy);
	}

	/**
   * @return database
   */
  public String getDatabase() {
    return getProperties().getByMethod("");
  }

  /**
   * @param database
   */
  public void setDatabase(String database) {
  	getProperties().setByMethod(database);
  }

  /**
   * @return port
   */
  public int getPort() {
  	return getProperties().getByMethod(3306);
  }

  /**
   * @param port
   */
  public void setPort(int port) {
    getProperties().setByMethod(port);
  }

  public String getEncoding() {
  	return getProperties().getByMethod("GBK");
  }

  public void setEncoding(String encoding) {
  	getProperties().setByMethod(encoding);
  }
}
