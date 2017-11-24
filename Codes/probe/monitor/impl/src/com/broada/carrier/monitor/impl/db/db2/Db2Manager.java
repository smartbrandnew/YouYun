package com.broada.carrier.monitor.impl.db.db2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.db2.DB2ConfPanel;
import com.broada.carrier.monitor.method.db2.DB2MonitorMethodOption;
import com.broada.utils.DateUtil;
import com.broada.utils.JDBCUtil;
import com.broada.utils.StringUtil;

/** 
 * Db2Manager
 * @author lixy (lixy@broada.com.cn)
 * Create By 2007-3-22 下午05:12:00
 */
public class Db2Manager {
	private static String sql = "SELECT count(*) FROM TABLE( SNAPSHOT_DBM(-1)) as SNAPSHOT_DBM";
	private static String sql_new = "SELECT count(*) FROM TABLE( SNAP_GET_DBM(-1)) as SNAP_GET_DBM";
  private static final String DB2_SQL_DB_INFO = "select db_status, db_conn_time, appls_cur_cons, total_cons,SQLM_ELM_LAST_BACKUP  from TABLE (SNAPSHOT_DATABASE ( '#db_sid#', -1)) as SNAPSHOT_DATABASE";
  private static final String DB2_SQL_DB_INFO_NEW = "select db_status, db_conn_time, appls_cur_cons, total_cons, null as SQLM_ELM_LAST_BACKUP  from TABLE (SNAP_GET_DB ( '#db_sid#', -1)) as SNAP_GET_DB";
  protected String ip;
  protected DB2MonitorMethodOption option;

  public Db2Manager(String ip, DB2MonitorMethodOption option) {
    this.ip = ip;
    this.option = option;
  }

  /**
   * 获取数据库基本信息对象
   * @return
   */
  public Map<String, Object> getDb2BaseInfo(Connection conn) throws SQLException,CLIException {
    Map<String, Object> db2Base = new HashMap<String, Object>();
    String db2_sql_db_info = (option.ifNewVersion() ? DB2_SQL_DB_INFO_NEW : DB2_SQL_DB_INFO).replaceAll("#db_sid#", option.getDb());
    if (option.getOptType().equalsIgnoreCase(DB2MonitorMethodOption.CLI4DB2MONITORMETHOD)) {
      List result = new DB2AgentExecutor(ip).execute(ip, option, CLIConstant.DB2AGENT,
          new String[] { option.getUsername(), option.getDb(), db2_sql_db_info });
      if (result != null && result.size() > 0) {
      	Object temp = result.get(0);
      	if(temp instanceof Map){
	        Map map = (Map) result.get(0);
	        db2Base.put(Db2ParamConfiger.keys[0], praseDb2DbStatus(map.get("DB_STATUS") == null ? Integer.valueOf(map.get(
	            "DB_STATUS").toString()) : 0));
	        String db_conn_time = (map.get("DB_CONN_TIME") != null ? map.get("DB_CONN_TIME").toString() : "");
	        db2Base.put(Db2ParamConfiger.keys[1], db_conn_time);
	        db2Base.put(Db2ParamConfiger.keys[2], new Integer(map.get("APPLS_CUR_CONS") == null ? Integer.valueOf(map.get(
	            "APPLS_CUR_CONS").toString()) : 0));
	        db2Base.put(Db2ParamConfiger.keys[3], new Integer(map.get("TOTAL_CONS") == null ? Integer.valueOf(map.get(
	            "TOTAL_CONS").toString()) : 0));
	        String sqlm_elm_last_backup = map.get("SQLM_ELM_LAST_BACKUP") != null ? map.get("SQLM_ELM_LAST_BACKUP")
	            .toString() : "";
	        db2Base.put(Db2ParamConfiger.keys[4], sqlm_elm_last_backup);
	      } else if (temp instanceof String){
	      	DB2CmdTable table = new DB2CmdTable((String)temp);
	      	String DB_STATUS = table.getData(0, table.getColumnIndex("DB_STATUS"));
	      	String DB_CONN_TIME = table.getData(0, table.getColumnIndex("DB_CONN_TIME"));
	      	String APPLS_CUR_CONS = table.getData(0, table.getColumnIndex("APPLS_CUR_CONS"));
	      	String TOTAL_CONS = table.getData(0, table.getColumnIndex("TOTAL_CONS"));
	      	String SQLM_ELM_LAST_BACKUP = table.getData(0, table.getColumnIndex("SQLM_ELM_LAST_BACKUP"));
	      	
	      	db2Base.put(Db2ParamConfiger.keys[0], praseDb2DbStatus(DB_STATUS != null ? Integer.valueOf(DB_STATUS) : 0));
	        db2Base.put(Db2ParamConfiger.keys[1], DB_CONN_TIME != null ? DB_CONN_TIME : "");
	        db2Base.put(Db2ParamConfiger.keys[2], APPLS_CUR_CONS != null ? Integer.valueOf(APPLS_CUR_CONS) : 0);
	        db2Base.put(Db2ParamConfiger.keys[3], TOTAL_CONS != null ? Integer.valueOf(TOTAL_CONS) : 0);
	        db2Base.put(Db2ParamConfiger.keys[4], SQLM_ELM_LAST_BACKUP != null ? TOTAL_CONS : 0);
	      } else 
	      	throw new IllegalArgumentException(String.format("无法识别的数据类型：%s", temp)); 
      }
      return db2Base;
    }
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
      ps = conn.prepareStatement(db2_sql_db_info);
      rs = ps.executeQuery();
      if (rs.next()) {
      	String db_status = rs.getString("db_status");
      	try{
      		int status = Integer.parseInt(db_status);
      		db2Base.put(Db2ParamConfiger.keys[0], praseDb2DbStatus(status));
      	}catch(NumberFormatException e){
      		db2Base.put(Db2ParamConfiger.keys[0], db_status);
      	}
        db2Base.put(Db2ParamConfiger.keys[1], DateUtil.DATETIME_FORMAT.format(new Date(rs.getTimestamp("db_conn_time")
            .getTime())));
        db2Base.put(Db2ParamConfiger.keys[2], new Integer(rs.getInt("appls_cur_cons")));
        db2Base.put(Db2ParamConfiger.keys[3], new Integer(rs.getInt("total_cons")));
        if (rs.getTimestamp("SQLM_ELM_LAST_BACKUP") == null) {
          db2Base.put(Db2ParamConfiger.keys[4], "");
        } else {
          db2Base.put(Db2ParamConfiger.keys[4], DateUtil.DATETIME_FORMAT.format(new Date(rs
              .getTimestamp("SQLM_ELM_LAST_BACKUP").getTime())));
        }
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      JDBCUtil.close(rs, ps);
    }
    return db2Base;
  }

  public Connection getConnection() throws SQLException, CLIException {
    if (option.getOptType().equalsIgnoreCase(DB2MonitorMethodOption.CLI4DB2MONITORMETHOD)) {
      List result = new DB2AgentExecutor(ip).execute(ip, option, CLIConstant.DB2AGENT,
          new String[] { option.getUsername(), option.getDb(), option.ifNewVersion()? sql_new : sql });
      if (result != null && result.size() > 0) {
      }
      return null;
    }
    Connection conn = getConnectionIgnoreRole();

    // 检查该用户是否有SYSADM、SYSCTRL 或 SYSMAINT 权限,当可访问快照,表示有该权限；否则,无该权限.

    Statement stm = null;
    boolean hasRole = false;
    try {
      stm = conn.createStatement();
      stm.execute(option.ifNewVersion()? sql_new : sql);
      hasRole = true;
    } catch (SQLException ex) {
    } finally {
      JDBCUtil.close(stm);
    }

    if (!hasRole) {
      JDBCUtil.close(conn);
      throw new SQLException("指定用户无SYSADM、SYSCTRL 或 SYSMAINT 权限.");
    }
    return conn;
  }

  public Connection getConnectionIgnoreRole() throws SQLException {
    if (option.getUsername() == null || option.getUsername().equals("")) {
      throw new SQLException("未指定数据库用户.");
    }

    Connection conn = null;
    int errCode = 0; // 0表示正常

    if (option.getDb() == null || option.getDb().length() == 0) {
      option.setDb("db2");
    }

    String url = "";
    String driver = "";
    if (StringUtil.isNullOrBlank(option.getDriverType()) || option.getDriverType().equalsIgnoreCase(DB2ConfPanel.NORMAL)) {
      driver = DB2ConfPanel.DRIVER_NORMAL;
      url = DB2ConfPanel.URL_NORMAL;
    } else if (option.getDriverType().equalsIgnoreCase(DB2ConfPanel.AS400)) {
      driver = DB2ConfPanel.DRIVER_AS400;
      url = DB2ConfPanel.URL_AS400;
    }

    String realUrl = MessageFormat.format(url, new Object[] { ip, String.valueOf(option.getPort()), option.getDb() });
    Exception exception = null;
    try {
      conn = JDBCUtil.createConnection(driver, realUrl, option.getUsername(), option.getPassword());
    } catch (ClassNotFoundException ex) {
      throw new SQLException("无法获取DB2驱动.");
    } catch (SQLException ex) {
      errCode = ex.getErrorCode();
      exception = ex;
      JDBCUtil.close(conn);
    }

    // 根据错误号来判断错误类型
    // -4499数据库url错误
    // -99999 用户名或密码错误
    if (errCode == -4499) {
      SQLException se = new SQLException("连接失败,数据库url错误,可能指定的端口不是DB2监听端口或数据库实例名错误.");
      se.initCause(exception);
      throw se;
    } else if (errCode == -99999) {
      SQLException se = new SQLException("连接失败,用户名或密码错误.", "", errCode);
      se.initCause(exception);
      throw se;
    } else if (errCode != 0) {
      SQLException se = new SQLException("数据库没有装载或打开,错误:" + exception.getMessage());
      se.initCause(exception);
      throw se;
    } else if (exception != null) {//如果有未知异常则继续抛出
      SQLException se = new SQLException("连接失败,请查看实例名或相关配置是否正确.");
      se.initCause(exception);
      throw se;
    } else if (conn == null) {//判断连接是否有效
      SQLException se = new SQLException("连接失败,请检查配置是否正确.");
      throw se;
    }

    return conn;
  }

  private String praseDb2DbStatus(int dbStatus) {
    switch (dbStatus) {
    case 0:
      return "活动";
    case 1:
      return "停顿－暂挂";
    case 2:
      return "已停顿";
    case 3:
      return "rollforward ";
    default:
      break;
    }
    return "";
  }

  public DB2MonitorMethodOption getOption() {
    return option;
  }

  public void setOption(DB2MonitorMethodOption option) {
    this.option = option;
  }
}
