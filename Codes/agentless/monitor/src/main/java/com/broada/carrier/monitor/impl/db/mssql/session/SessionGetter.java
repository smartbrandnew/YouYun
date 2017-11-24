package com.broada.carrier.monitor.impl.db.mssql.session;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.db.mssql.DataAccessException;
import com.broada.carrier.monitor.method.mssql.MSSQLMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.common.db.DBUtil;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.utils.DateUtil;

public class SessionGetter {
  private static final Log logger = LogFactory.getLog(SessionGetter.class);
  private static final String DRIVER = "net.sourceforge.jtds.jdbc.Driver";

	private static final String URL = "jdbc:jtds:sqlserver://{0}:{1};databaseName={2}";

	private static final String URL_DOMAIN = "jdbc:jtds:sqlserver://{0}:{1};domain={2}&databaseName={3}";
	private static final String SQL_SESSIONS1 = "SELECT spid 'PID', rtrim(status) 'Status', SUSER_SNAME(sid) 'User', rtrim(hostname) 'Host', rtrim(program_name) 'Program', memusage 'Mem_Usage', cpu 'CPU_Time', 'Database'= CASE WHEN dbid=0 THEN '[NULL]' ELSE DB_NAME(dbid) END, cmd 'Command', Last_Batch, Login_Time FROM master.dbo.sysprocesses (nolock) ORDER BY 2";
	private static final String SQL_SESSIONS2 = "SELECT spid 'PID', rtrim(status) 'Status', SUSER_SNAME(sid) 'User', rtrim(hostname) 'Host', rtrim(program_name) 'Program', memusage 'Mem_Usage', cpu 'CPU_Time', 'Database'= CASE WHEN dbid=0 THEN '[NULL]' ELSE DB_NAME(dbid) END, cmd 'Command' FROM master.dbo.sysprocesses (nolock) ORDER BY 2";
  
  static{
    try {
      Class.forName(DRIVER);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  public static List getSessions(String ip, MSSQLMonitorMethodOption option) throws SQLException{
    String host = ip;
    int port = option.getPort();
    String username = option.getUsername();
    String password = option.getPassword();
    String domain = option.getDomain();
    String instanceName = option.getInstanceName();
    if (instanceName == null)
      instanceName = "";
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    
    String sql = SQL_SESSIONS1;
    
    while (true) {
	    List sessions = new ArrayList();
	    try {
	      DriverManager.setLoginTimeout(30);
	      String url;
	      if (StringUtils.isEmpty(domain)) 
	        url = MessageFormat.format(URL, new Object[]{host, String.valueOf(port), instanceName});
	      else
	        url = MessageFormat.format(URL_DOMAIN, new Object[]{host, String.valueOf(port), domain, instanceName});
	      conn = DriverManager.getConnection(url, username, password);
	      stmt = conn.createStatement();
	      rs = stmt.executeQuery(sql);
	      while(rs.next()){
	        SessionInfo info = new SessionInfo();
	        info.setId(rs.getString("PID"));
	        info.setStatus(rs.getString("Status"));
	        info.setUser(rs.getString("User"));
	        info.setHost(rs.getString("Host"));
	        info.setProgram(rs.getString("Program"));
	        info.setMemory(rs.getInt("Mem_Usage"));
	        info.setCpuTime(rs.getInt("CPU_Time"));
	        info.setCommand(rs.getString("Command"));
	        info.setDatabase(rs.getString("Database"));
	        if (sql.equals(SQL_SESSIONS1)) {
		        info.setLastBatchTime(DateUtil.DATETIME_FORMAT.format(rs.getTimestamp("Last_Batch")));
		        info.setLoginTime(DateUtil.DATETIME_FORMAT.format(rs.getTimestamp("Login_Time")));
	        }
	        sessions.add(info);
	      }
	      return sessions;
	    } catch (SQLException e) {
	    	if (e.getMessage().contains("Last_Batch")) {
	    		ErrorUtil.warn(logger, "监测目标不支持Last_Batch属性，将放弃此属性", e);
	    		sql = SQL_SESSIONS2;
	    	} else
	    		throw e;
	    } finally{
	    	DBUtil.close(rs, stmt, conn);
	    }	    
    }
  }
 
  public static List getSessions(String ip, MonitorMethod method) throws DataAccessException{    
    MSSQLMonitorMethodOption option = new MSSQLMonitorMethodOption(method);
    try {
      return getSessions(ip, option);
    } catch (SQLException e) {
      throw new DataAccessException("获取会话发生错误" ,e);
    }
  }
}
