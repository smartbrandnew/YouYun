package com.broada.carrier.monitor.method.postgresql;

import com.broada.carrier.monitor.method.common.JdbcMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class PostgreSQLMonitorMethodOption extends JdbcMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolPostgreSQL";
	
  public PostgreSQLMonitorMethodOption() {
		super();
	}

	public PostgreSQLMonitorMethodOption(MonitorMethod copy) {
		super(copy);
	}

	/**
   * @return database
   */
  public String getDb() {
  	return getProperties().getByMethod("postgres");
  }

  /**
   * @param database
   */
  public void setDb(String database) {
  	getProperties().setByMethod(database);
  }
  
  /**
   * @return port
   */
  public int getPort() {
  	return getProperties().getByMethod(5432);
  }

  /**
   * @param port
   */
  public void setPort(int port) {
  	getProperties().setByMethod(port);
  }
}
