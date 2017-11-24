package com.broada.carrier.monitor.impl.db.mssql.basic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.db.mssql.DataAccessException;
import com.broada.carrier.monitor.method.mssql.MSSQLMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.component.utils.error.ErrorUtil;

public class BasicInfoGetter {
  private static final Log logger = LogFactory.getLog(BasicInfoGetter.class);
  private static final String DRIVER = "net.sourceforge.jtds.jdbc.Driver";

  private static final String URL = "jdbc:jtds:sqlserver://{0}:{1};databaseName={2}";
  
  private static final String URL_DOMAIN = "jdbc:jtds:sqlserver://{0}:{1};domain={2}&databaseName={3}";
  
  private static final String BASIC_SQL_VERSION = "SELECT SERVERPROPERTY('productversion') as value";
  
  static{
    try {
      Class.forName(DRIVER);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  public static List<BasicInfo> getBasicInfos(String ip, MonitorMethod method) throws DataAccessException{    
    MSSQLMonitorMethodOption option = new MSSQLMonitorMethodOption(method);
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
    
    List<BasicInfo> basicInfos = new ArrayList<BasicInfo>();
    try {
      DriverManager.setLoginTimeout(30);
      String url;
      if (StringUtils.isEmpty(domain)) 
        url = MessageFormat.format(URL, new Object[]{host, String.valueOf(port), instanceName});
      else
        url = MessageFormat.format(URL_DOMAIN, new Object[]{host, String.valueOf(port), domain, instanceName});
      conn = DriverManager.getConnection(url, username, password);
      
      stmt = conn.createStatement();
      
      rs = stmt.executeQuery("EXEC('master.dbo.xp_msver')");
      fill(basicInfos, rs, "Name", "Character_Value", new BasicInfo[]{
      		new BasicInfo("WindowsVersion", "", 1),
      		new BasicInfo("ProcessorType", "", 2),
      		new BasicInfo("ProcessorCount", 0, 3),
      		new BasicInfo("PhysicalMemory", "", 5),
      });
      
      rs = stmt.executeQuery("SELECT host = @@SERVERNAME");
      fill(basicInfos, rs, "host", new BasicInfo("主机名", "", 4));
      
      rs = stmt.executeQuery("SELECT (SELECT crdate FROM master..sysdatabases WITH (nolock) WHERE name = 'tempdb') as value");      
      fill(basicInfos, rs, "value", new BasicInfo("启动时间", "", 8));
      
      rs = stmt.executeQuery("SELECT 'number_of_databases' as code, (SELECT COUNT(*) FROM master..sysdatabases WITH (nolock )) as value " + 
      		"union " + 
      		"SELECT 'connected_processes' as code, (SELECT COUNT(*) FROM master..sysprocesses WITH (nolock )) as value " + 
      		"union " + 
      		"SELECT 'blocked_processes' as code, (SELECT COUNT(* ) FROM master..sysprocesses WITH (nolock ) WHERE blocked <> 0) as value " + 
      		"union " + 
      		"SELECT 'open_transactions' as code, (SELECT COUNT(*) FROM master..sysprocesses WITH (nolock) WHERE open_tran <> 0) as value");
      fill(basicInfos, rs, "code", "value", new BasicInfo[]{
      		new BasicInfo("number_of_databases", 0, 6),
      		new BasicInfo("connected_processes", 0, 7),
      		new BasicInfo("blocked_processes", 0, 9),
      });
      
      rs = stmt.executeQuery(BASIC_SQL_VERSION);
      fill(basicInfos, rs, "value", new BasicInfo("数据库版本", "", 10));
           
      rs = stmt.executeQuery("select sum(size*8/1024) as size from sysfiles where fileid = 1 ");
      fill(basicInfos, rs, "size", new BasicInfo("数据文件大小", 0.0, 11));
      
      rs = stmt.executeQuery("select sum(size*8/1024) as size from sysfiles where fileid = 2 ");
      fill(basicInfos, rs, "size", new BasicInfo("日志文件大小", 0.0, 12));
                 
      try {
	      rs = stmt.executeQuery("exec sp_monitor ");
	      int size = 0;
	      if (stmt.getMoreResults()) {
		      rs = stmt.getResultSet();
		      if (rs.next()) {
		      	String str = rs.getString(1);
		        str = str.substring(str.indexOf("-") + 1, str.length() - 1);	        
		        size = Integer.parseInt(str);
		      }
	      }
	            
	      int cpuNum = 1;
	      if (size > 0) {
	      	for (BasicInfo info : basicInfos) {
	      		if (info.getName().equalsIgnoreCase("ProcessorCount")) {
	      			cpuNum = (Integer) info.getValue();
	      			break;
	      		}
	      	}      	
		      if (cpuNum <= 0)
		      	cpuNum = 1;
	      }
	      double cpuUsage = ((int)(size * 10.0 / cpuNum) / 10);
	      if (cpuUsage > 100)
	      	cpuUsage = 100;
	      BasicInfo info = new BasicInfo("CPU使用率", cpuUsage, 13);
	    	basicInfos.add(info);
	      rs.close(); 
      } catch (Throwable e) {
      	ErrorUtil.warn(logger, "获取基本信息CPU利用率失败", e);
      }
	      
      rs = stmt.executeQuery("select count(*) as value from master.dbo.sysprocesses where hostprocess > 0 ");
      fill(basicInfos, rs, "value", new BasicInfo("连接会话数", 0, 14));
    } catch (SQLException e) {
    	throw new DataAccessException("获取基本信息出错", e);
    }finally{
      if(rs != null){
        try {
          rs.close();
        } catch (SQLException e) {
          logger.error("关闭rs出错", e);
        }
      }
      
      if(stmt != null){
        try {
          stmt.close();
        } catch (SQLException e) {
          logger.error("关闭stmt出错", e);
        }
      }
      try {
        if(conn != null && !conn.isClosed()){
          conn.close();
        }
      }catch(SQLException e){
        logger.error("关闭stmt出错", e);
      }
    }
    Collections.sort(basicInfos, new Comparator<BasicInfo>(){
      public int compare(BasicInfo b1, BasicInfo b2) {        
        return b1.getSort() - b2.getSort() > 0 ? 1 : -1;
      }
      
    });
    return basicInfos;
  }
  
	private static void fill(List<BasicInfo> infos, ResultSet rs, String valueField, BasicInfo cond) {
		try {
	    if (rs.next()) {    	
	    	fillOne(infos, rs, valueField, cond);  			
	    }
		} catch (Throwable e) {
			ErrorUtil.warn(logger, "获取SQLServer指标失败", e);
		} finally {
			try {
				rs.close();
			} catch (Throwable e) {
				ErrorUtil.warn(logger, "关闭会话失败", e);
			}
		}
	}

	private static void fillOne(List<BasicInfo> infos, ResultSet rs, String valueField, BasicInfo cond) throws SQLException {
		Object value;
		if (cond.getValue() instanceof String)
			value = rs.getString(valueField);
		else if (cond.getValue() instanceof Integer)
			value = rs.getInt(valueField);
		else if (cond.getValue() instanceof Float || cond.getValue() instanceof Double)
			value = rs.getDouble(valueField);		
		else 
			throw new IllegalArgumentException();
		infos.add(new BasicInfo(cond.getName(), value, cond.getSort()));
	}

	private static void fill(List<BasicInfo> infos, ResultSet rs, String keyField, String valueField,
			BasicInfo[] conds) {
		try {
	    while (rs.next()) {    	
	    	String key = rs.getString(keyField);
	    	for (BasicInfo cond : conds) {
	    		if (cond.getName().equalsIgnoreCase(key)) {
	    			fillOne(infos, rs, valueField, cond);
	    		}
	    	}            
	    }
		} catch (Throwable e) {
			ErrorUtil.warn(logger, "获取SQLServer指标失败", e);
		} finally {
			try {
				rs.close();
			} catch (Throwable e) {
				ErrorUtil.warn(logger, "关闭会话失败", e);
			}
		}
	}  
}
