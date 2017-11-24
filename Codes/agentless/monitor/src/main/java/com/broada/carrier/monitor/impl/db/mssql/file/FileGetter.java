package com.broada.carrier.monitor.impl.db.mssql.file;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.db.mssql.DataAccessException;
import com.broada.carrier.monitor.method.mssql.MSSQLMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class FileGetter {
  private static final Log logger = LogFactory.getLog(FileGetter.class);
  private static final String DRIVER = "net.sourceforge.jtds.jdbc.Driver";
  private static final String SQL = "SELECT groupname,name,size*8.0/1024 size,maxsize,'growth' = case when (A.status&0x100000)=0x100000 then growth else growth*8.0/1024 end,'growType'=case when (A.status&0x100000)=0x100000 then 0 else 1 end,filename FROM dbo.sysfiles A,dbo.sysfilegroups B where A.groupid=B.groupid union SELECT 'Log' groupname,name,size*8.0/1024 size,maxsize,'growth' = case when (A.status&0x100000)=0x100000 then growth else growth*8.0/1024 end,'growType'=case when (A.status&0x100000)=0x100000 then 0 else 1 end,filename FROM dbo.sysfiles A where not exists(select * from dbo.sysfilegroups B where A.groupid=B.groupid)";

	private static final String URL = "jdbc:jtds:sqlserver://{0}:{1};databaseName={2}";

	private static final String URL_DOMAIN = "jdbc:jtds:sqlserver://{0}:{1};domain={2}&databaseName={3}";

  static{
    try {
      Class.forName(DRIVER);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  public static List getFiles(String ip, MonitorMethod method) throws DataAccessException{
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
    NumberFormat format = NumberFormat.getInstance();
    format.setMaximumFractionDigits(2);
    format.setMinimumFractionDigits(2);
    format.setMinimumIntegerDigits(1);
    
    List files = new ArrayList();
    try {
      DriverManager.setLoginTimeout(30);
      String url;
      if (StringUtils.isEmpty(domain)) 
        url = MessageFormat.format(URL, new Object[]{host, String.valueOf(port), instanceName});
      else
        url = MessageFormat.format(URL_DOMAIN, new Object[]{host, String.valueOf(port), domain, instanceName});
      conn = DriverManager.getConnection(url, username, password);
      stmt = conn.createStatement();
      rs = stmt.executeQuery("exec sp_databases");
      
      while(rs.next()){
        String database = rs.getString("DATABASE_NAME");
       
        Statement stmt2 = null;
        ResultSet rs2 = null;
        try{
          stmt2 = conn.createStatement();
          stmt2.execute("use [" + database + "]");
          rs2 = stmt2.executeQuery(SQL);
          while(rs2.next()){
            FileInfo info = new FileInfo();
            info.setDatabaseName(database);
            info.setName(rs2.getString("name").trim());
            info.setGroupName(rs2.getString("groupname"));
            info.setSize(rs2.getFloat("size"));
            int maxsize = rs2.getInt("maxsize");
            if(maxsize == 0){
              info.setMaxCapability(format.format(info.getSize()));
            }else if(maxsize == -1){
              info.setMaxCapability("不限");
            }else{
              info.setMaxCapability(format.format(maxsize*8.0/1024) + "MB");
            }
            int growType = rs2.getInt("growType");
            info.setGrowth(format.format(rs2.getFloat("growth")) + (growType == 0 ? "%" : "MB"));
            info.setFileName(rs2.getString("fileName"));
            
            files.add(info);
          }
        }catch(Exception e){
          throw new DataAccessException("获取文件信息出错:"+e.getMessage(), e);
        }finally{
          if(rs2 != null){
            try {
              rs2.close();
            } catch (SQLException e) {
              logger.error("关闭rs2出错", e);
            }
          }
          if(stmt2 != null){
            try {
              stmt2.close();
            } catch (SQLException e) {
              logger.error("关闭stmt2出错", e);
            }
          }
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("获取文件信息出错", e);
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
    return files;
  }
}
