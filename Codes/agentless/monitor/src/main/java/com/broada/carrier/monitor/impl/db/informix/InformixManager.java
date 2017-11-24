package com.broada.carrier.monitor.impl.db.informix;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.broada.carrier.monitor.impl.db.informix.dbspace.InformixDataBaseSpace;
import com.broada.carrier.monitor.impl.db.informix.strategy.InformixStrategyFacade;
import com.broada.carrier.monitor.impl.db.informix.strategy.entity.InformixStrategyResult;
import com.broada.carrier.monitor.impl.db.informix.tbrecord.InformixTableRecord;
import com.broada.carrier.monitor.method.informix.InformixMonitorMethodOption;
import com.broada.utils.JDBCUtil;

/**
 * <p>Title: InformixManager</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 2.4
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class InformixManager {
  private String ip;

  private int port;

  private String serverName;

  private String user;

  private String pass;

  private Connection conn;

  public static final String INFORMIX_SQL_CONNECTS = "select count(*) as connects from syssessions";

  public static final String INFORMIX_SQL_BUFREADS = "select value as bufreads from sysprofile where name = 'bufreads'";

  public static final String INFORMIX_SQL_BUFWRITES = "select value as bufwrites from sysprofile where name = 'bufwrites'";

  public static final String INFORMIX_SQL_DSKREADS = "select value as dskreads from sysprofile where name = 'dskreads'";

  public static final String INFORMIX_SQL_DSKWRITES = "select value as dskwrites from sysprofile where name = 'dskwrites'";

  public static final String INFORMIX_SQL_DEADLOCK = "select value as deadlock from sysprofile where name = 'deadlks'";

  public static final String INFORMIX_SQL_ROLLBACK = "select sum(isrollbacks) as rollback from syssesprof";

  //  public static final String INFORMIX_SQL_DBSPACE = "select space.name as name, 100*chunk.nfree/chunk.chksize as perDBS  "
  //      + "from sysdbspaces space, syschunks chunk " + "where space.dbsnum = chunk.dbsnum";
  public static final String INFORMIX_SQL_DBSPACE = "select name,round(((sum(chksize)-sum(nfree))/sum(chksize))*100, 2) perDBS "
      + "from sysdbspaces d,syschunks c " + "where d.dbsnum=c.dbsnum group by name order by 2 desc";

  public static final String INFORMIX_SQL_DATABASE = "select name from sysdatabases";

  public static final String INFORMIX_SQL_TABLES = "select tabname, tabtype from systables where tabtype = 'T' or tabtype = 'V'";
  
  public InformixManager(String ip, InformixMonitorMethodOption method) {
  	this(ip, method.getPort(), method.getServername(), method.getUsername(), method.getPassword());
  }

  public InformixManager(String ip, int port, String serverName, String user, String pass) {
    this.ip = ip;
    this.port = port;
    this.serverName = serverName;
    this.user = user;
    this.pass = pass;
  }

  /**
   * 初始化链接
   * @throws SQLException
   */
  public void initConnection() throws SQLException {
    if (this.conn == null) {
      this.conn = getConnection("sysmaster");
    }
  }

  /**
   * 获取数据库链接
   * @return
   * @throws SQLException
   */
  private Connection getConnection() throws SQLException {
    if (conn == null) {
      throw new NullPointerException("数据库链接还没有初始化,请先初始化.");
    }
    return conn;
  }

  private Connection getConnection(String dataBaseName) throws SQLException {
    if (dataBaseName == null || dataBaseName.equals("")) {
      throw new SQLException("未指定数据库.");
    }

    if (user == null || user.equals("")) {
      throw new SQLException("未指定数据库用户.");
    }

    Connection conn = null;
    Exception e = null;
    int errCode = 0; // 0表示正常

    String url = "jdbc:informix-sqli://" + this.ip + ":" + this.port + "/" + dataBaseName + ":" + "informixServer="
        + this.serverName;
    try {
      conn = JDBCUtil.createConnection("com.informix.jdbc.IfxDriver", url, user, pass);
    } catch (ClassNotFoundException ex) {
      throw new SQLException("无法获取Informix驱动.");
    } catch (SQLException ex) {
      errCode = ex.getErrorCode();
      e = ex;
      JDBCUtil.close(conn);
    }

    if (errCode == -908) {
      SQLException sqle = new SQLException("无法与数据库建立连接,请检查配置!");
      sqle.initCause(e);
      throw sqle;
    }

    if (errCode == -951) {
      SQLException sqle = new SQLException("用户名或密码不正确,请重新输入!");
      sqle.initCause(e);
      throw sqle;
    }

    if (errCode == -761) {
      SQLException sqle = new SQLException("无效的数据库服务名(dbservername)!");
      sqle.initCause(e);
      throw sqle;
    }

    if (errCode == -329) {
      SQLException sqle = new SQLException("数据库不存在或没有系统权限!");
      sqle.initCause(e);
      throw sqle;
    }

    if (errCode == -387) {
      SQLException sqle = new SQLException("指定用户没有连接(connect)权限!");
      sqle.initCause(e);
      throw sqle;
    }
    if(conn==null){
      SQLException sqle = new SQLException("建立数据库链接失败!");
      sqle.initCause(e);
      throw sqle;
    }
    return conn;
  }

  /**
   * 获取连接数
   * 
   * @return
   * @throws SQLException
   */
  public int getConnects() throws SQLException {
    int connects = 0;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      Connection conn = getConnection();
      ps = conn.prepareStatement(INFORMIX_SQL_CONNECTS);
      rs = ps.executeQuery();
      while (rs.next()) {
        connects = rs.getInt("connects");
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      JDBCUtil.close(rs, ps);
    }
    return connects;
  }

  /**
   * 获取读缓存命中率
   * 
   * @return
   * @throws SQLException
   */
  public double getBufReadRatio() throws SQLException {
    double bufreads = 0, dskreads = 0;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      Connection conn = getConnection();
      ps = conn.prepareStatement(INFORMIX_SQL_BUFREADS);
      rs = ps.executeQuery();
      while (rs.next()) {
        bufreads = rs.getDouble("bufreads");
      }
      ps = conn.prepareStatement(INFORMIX_SQL_DSKREADS);
      rs = ps.executeQuery();
      while (rs.next()) {
        dskreads = rs.getDouble("dskreads");
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      JDBCUtil.close(rs, ps);
    }
    if (bufreads == 0 || bufreads < dskreads) {
      return 0;
    }
    return 100 * (bufreads - dskreads) / bufreads;
  }

  /**
   * 获取写缓存命中率
   * 
   * @return
   * @throws SQLException
   */
  public double getBufWriteRatio() throws SQLException {
    double bufwrites = 0, dskwrites = 0;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      Connection conn = getConnection();
      ps = conn.prepareStatement(INFORMIX_SQL_BUFWRITES);
      rs = ps.executeQuery();
      while (rs.next()) {
        bufwrites = rs.getDouble("bufwrites");
      }
      ps = conn.prepareStatement(INFORMIX_SQL_DSKWRITES);
      rs = ps.executeQuery();
      while (rs.next()) {
        dskwrites = rs.getDouble("dskwrites");
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      JDBCUtil.close(rs, ps);
    }

    if (bufwrites == 0 || bufwrites < dskwrites) {
      return 0;
    }
    return 100 * (bufwrites - dskwrites) / bufwrites;
  }

  /**
   * 获取死锁数量
   * 
   * @return
   * @throws SQLException
   */
  public int getDeadLocks() throws SQLException {
    int deadLocks = 0;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      Connection conn = getConnection();
      ps = conn.prepareStatement(INFORMIX_SQL_DEADLOCK);
      rs = ps.executeQuery();
      while (rs.next()) {
        deadLocks = rs.getInt("deadlock");
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      JDBCUtil.close(rs, ps);
    }

    return deadLocks;
  }

  /**
   * 获取回滚数
   * 
   * @return
   * @throws Exception
   */
  public int getRollBacks() throws SQLException {
    int rollbacks = 0;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      Connection conn = getConnection();
      ps = conn.prepareStatement(INFORMIX_SQL_ROLLBACK);
      rs = ps.executeQuery();
      while (rs.next()) {
        rollbacks = rs.getInt("rollback");
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      JDBCUtil.close(rs, ps);
    }

    return rollbacks;
  }

  /**
   * 获取数据库空间信息
   * 
   * @return
   * @throws SQLException
   */
  
public List getDataBaseSpaces() throws SQLException {
    List dbSpaces = new ArrayList();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      Connection conn = getConnection();
      ps = conn.prepareStatement(INFORMIX_SQL_DBSPACE);
      rs = ps.executeQuery();
      while (rs.next()) {
        InformixDataBaseSpace dbs = new InformixDataBaseSpace();
        dbs.setName(rs.getString("name").trim());
        dbs.setCurPerDBS(new Double(rs.getDouble("perDBS")));
        dbSpaces.add(dbs);
      }
    } finally {
      JDBCUtil.close(rs, ps);
    }
    return dbSpaces;
  }

  /**
   * 获取数据库数组
   * 
   * @return
   * @throws SQLException
   */
  public String[] getAllDataBases() throws SQLException {
    List dbList = new ArrayList();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      Connection conn = getConnection();
      ps = conn.prepareStatement(INFORMIX_SQL_DATABASE);
      rs = ps.executeQuery();
      while (rs.next()) {
        dbList.add(rs.getString("name"));
      }
    } finally {
      JDBCUtil.close(rs, ps);
    }

    return (String[]) dbList.toArray(new String[dbList.size()]);
  }

  /**
   * 获取指定数据库中的所有表信息(包括表名和表类型)
   * 
   * @param dbName
   * @return
   * @throws SQLException
   */
//TODO jiangjw 待监测器移植取消  
  public List getTablesByDbName(String dbName) throws SQLException {
    if (dbName == null || dbName.equals("")) {
      throw new SQLException("未指定数据库名!");
    }
    List tables = new ArrayList();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      Connection conn = getConnection(dbName);
      ps = conn.prepareStatement(INFORMIX_SQL_TABLES);
      rs = ps.executeQuery();
      while (rs.next()) {
        InformixTableRecord it = new InformixTableRecord();
        it.setName(rs.getString("tabname"));
        it.setType(rs.getString("tabtype"));
        tables.add(it);
      }
    } finally {
      JDBCUtil.close(rs, ps, conn);
    }

    return tables;
  }
  
  /**
   * 根据策略组ID获取策略结果集
   * 
   * @param strategyGroupId
   * @return
   * @throws SQLException
   */
  public InformixStrategyResult getStrategyResult(String strategyGroupId, String srvId) throws SQLException {
    return InformixStrategyFacade.getStrategyResult(getConnection(), strategyGroupId, srvId);
  }
  
  /**
   * 关闭并释放链接
   */
  public void close() {
    if (conn != null) {
      JDBCUtil.close(conn);
      conn=null;
    }
  }
}
