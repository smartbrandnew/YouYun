package com.broada.carrier.monitor.impl.db.mysql;

import java.sql.Connection;

/**
 * 负责创建MySQL连接
 * 
 * @author zhoucy (zhoucy@broada.com.cn)
 * @Email : zhoucy@broada.com
 * @Create By 2006-7-27 上午11:05:16
 */
public interface MySQLConnection {
  /**
   * 
   * @param url
   * @param user
   * @param password
   * @return
   * @throws MySQLException
   */
  public Connection connection(String url, String user, String password) throws MySQLException;

  /**
   * 动态组合connection url
   * 
   * @param host
   * @param port
   * @param database
   * @return
   */
  public String getUrl(String host, int port, String database,String encoding);
}
