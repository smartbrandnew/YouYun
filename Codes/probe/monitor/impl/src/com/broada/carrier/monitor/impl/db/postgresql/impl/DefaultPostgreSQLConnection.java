package com.broada.carrier.monitor.impl.db.postgresql.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;

import com.broada.carrier.monitor.impl.db.postgresql.PostgreSQLConnection;
import com.broada.carrier.monitor.impl.db.postgresql.PostgreSQLException;
import com.broada.utils.JDBCUtil;

public class DefaultPostgreSQLConnection implements PostgreSQLConnection {
  private final static String POSTGRESQL_DRIVER = "org.postgresql.Driver";

  private final static String JDBC_URL = "jdbc:postgresql://{0}:{1}/{2}";

  public Connection connection(String url, String user, String password) throws PostgreSQLException {
    try {
      return JDBCUtil.createConnection(POSTGRESQL_DRIVER, url, user, password);
    } catch (ClassNotFoundException e) {
      throw new PostgreSQLException("找不到PostgreSQL驱动", e);
    } catch (SQLException e) {
      throw new PostgreSQLException(e);
    }
  }

  public String getUrl(String host, int port, String database) {
    return MessageFormat.format(JDBC_URL, new Object[] { host, "" + port, database });
  }
}
