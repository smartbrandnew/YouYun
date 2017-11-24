package com.broada.carrier.monitor.impl.db.mysql.impl;

import com.broada.carrier.monitor.impl.db.mysql.MySQLConnection;
import com.broada.carrier.monitor.impl.db.mysql.MySQLException;
import com.broada.utils.JDBCUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;

public class DefaultMySQLConnection implements MySQLConnection {
  private final static String MYSQL_DRIVER = "org.gjt.mm.mysql.Driver";

  private final static String JDBC_URL = "jdbc:mysql://{0}:{1}/{2}?useUnicode=true&characterEncoding={3}";

  public Connection connection(String url, String user, String password) throws MySQLException {
    try {
      return JDBCUtil.createConnection(MYSQL_DRIVER, url, user, password);
    } catch (ClassNotFoundException e) {
      throw new MySQLException("找不到MySQL驱动", e);
    } catch (SQLException e) {
      throw new MySQLException(e);
    }
  }

  public String getUrl(String host, int port, String database,String encoding) {
    return MessageFormat.format(JDBC_URL, new Object[] { host, "" + port, database ,encoding});
  }
}
