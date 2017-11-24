package com.broada.carrier.monitor.impl.db.mysql.impl;

import com.broada.carrier.monitor.impl.db.mysql.MySQLDatabaseInfoRetriever;
import com.broada.utils.JDBCUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultMySQLDatabaseInfoRetriever implements MySQLDatabaseInfoRetriever {
  private static final Log logger = LogFactory.getLog(DefaultMySQLDatabaseInfoRetriever.class);

  private final static String SHOW_DATABASES = "SHOW DATABASES";

  private final static String SHOW_STATUS = "SHOW STATUS LIKE ''%{0}%''";

  private final static String SHOW_VARIABLES = "SHOW VARIABLES LIKE ''%{0}%''";

  private Connection connection = null;

  public DefaultMySQLDatabaseInfoRetriever(Connection connection) {
    this.connection=connection;
  }

  private Map showMySQLSysInfo(String show, String param) {
    Statement stmt = null;
    ResultSet rs = null;
    Map sysInfoMap = new HashMap();
    try {
      stmt = connection.createStatement();
      rs = stmt.executeQuery(MessageFormat.format(show, new Object[] { param }));
      while (rs.next()) {
        String varName = rs.getString("Variable_name");
        String value = rs.getString("Value");
        sysInfoMap.put(varName.toUpperCase(), value);
      }
    } catch (SQLException e) {
      logger.error("获取系统信息错误,SQL="+show+",param="+param,e);
      return null;
    } finally {
      JDBCUtil.close(rs, stmt);
    }
    return sysInfoMap;
  }

  public Map getAllStatus() {
    return showMySQLSysInfo(SHOW_STATUS, "");
  }

  public Map getAllVariable() {
    return showMySQLSysInfo(SHOW_VARIABLES, "");
  }

  public String getStatus(String statusName) {
    return (String) showMySQLSysInfo(SHOW_STATUS, statusName).get(statusName.toUpperCase());
  }

  public String getVariable(String varName) {
    return (String) showMySQLSysInfo(SHOW_VARIABLES, varName).get(varName.toUpperCase());
  }

  public List showDatabases() {
    Statement stmt = null;
    ResultSet rs =null;
    List databases = new ArrayList();
    try {
      stmt = connection.createStatement();
      rs = stmt.executeQuery(SHOW_DATABASES);
      while (rs.next()) {
        databases.add(rs.getString(1));
      }
    } catch (SQLException e) {
      logger.error("获取数据库列表失败.",e);
      return null;
    } finally {
      JDBCUtil.close(rs, stmt);
    }
    return databases;
  }
}
