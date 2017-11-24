package com.broada.carrier.monitor.impl.db.mysql.impl;

import com.broada.carrier.monitor.impl.db.mysql.MySQLTableInfoRetriever;
import com.broada.carrier.monitor.impl.db.mysql.TableStatus;
import com.broada.utils.JDBCUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class DefaultMySQLTableInfoRetriever implements MySQLTableInfoRetriever {
  private static final Log logger = LogFactory.getLog(DefaultMySQLTableInfoRetriever.class);

  private final static String SHOW_TABLE_STATUS = "SHOW TABLE STATUS FROM {0} LIKE ''{1}''";

  private Connection connection = null;

  public DefaultMySQLTableInfoRetriever(Connection connection) {
    this.connection=connection;
  }

  public TableStatus getTableStatusInfo(String databaseName, String tableName) {
    List tableStatusList = getTableStatusInfos(databaseName, tableName);

    if (tableStatusList != null && !tableStatusList.isEmpty()) {
      return (TableStatus) tableStatusList.get(0);
    }
    return null;
  }

  public List getTableStatusInfos(String databaseName) {
    return getTableStatusInfos(databaseName, "");
  }

  private List getTableStatusInfos(String databaseName, String tableName) {
    List tableStatusList = new ArrayList();
    Statement stmt = null;
    ResultSet rs =null;
    try {
      stmt = connection.createStatement();
      rs=stmt.executeQuery(MessageFormat
          .format(SHOW_TABLE_STATUS, new Object[] { databaseName, tableName }));
      while (rs.next()) {
        TableStatus tableStatus = new TableStatus();
        tableStatus.setName(rs.getString("NAME"));
        tableStatus.setEngine(rs.getString("ENGINE"));
        tableStatus.setVersion(rs.getString("VERSION"));
        tableStatus.setRows(rs.getLong("ROWS"));
        tableStatus.setDataLength(rs.getLong("DATA_LENGTH"));
        tableStatus.setIndexLength(rs.getLong("INDEX_LENGTH"));
        tableStatus.setCreateTime(rs.getTimestamp("CREATE_TIME"));
        tableStatus.setUpdateTime(rs.getTimestamp("UPDATE_TIME"));
        tableStatus.setCheckTime(rs.getTimestamp("CHECK_TIME"));
        tableStatus.setComment(rs.getString("COMMENT"));
        tableStatusList.add(tableStatus);
      }
    } catch (SQLException e) {
      logger.error("获取表状态失败,dbName="+databaseName+",tbName="+tableName,e);
      return null;
    } finally {
      JDBCUtil.close(rs, stmt);
    }
    return tableStatusList;
  }
}
