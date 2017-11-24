package com.broada.carrier.monitor.impl.db.postgresql;

import java.sql.Connection;

public interface PostgreSQLConnection {
  /**
   * 
   * @param url
   * @param user
   * @param password
   * @return
   * @throws PostgreSQLException
   */
  public Connection connection(String url, String user, String password) throws PostgreSQLException;

  /**
   * 动态组合connection url
   * 
   * @param host
   * @param port
   * @param database
   * @return
   */
  public String getUrl(String host, int port, String database);
}
