package com.broada.carrier.monitor.impl.db.sybase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.db.sybase.database.SybaseDatabase;
import com.broada.utils.JDBCUtil;

/**
 * 
 * @author lixy (lixy@broada.com.cn) Create By 2007-12-7 下午08:07:56
 */
public class SybaseManager {
  
  private Log logger = LogFactory.getLog(SybaseManager.class);
  
  private static final String SYBASE_DBS = "exec sp_helpdb";

  private static final String SYBASE_DB_SPACE_USED = "exec sp_helpdb ";

  private String ip;

  private String sid;

  private int port;

  private String user;

  private String passwd;

  private Connection conn = null;
  
  public SybaseManager(String ip, String sid, int port, String user, String passwd) {
    this.ip = ip;
    this.sid = sid;
    this.port = port;
    this.user = user;
    this.passwd = passwd;
  }

  public void initConnection() throws ClassNotFoundException, Exception {
    if (conn == null) {
      try {
        String url = "jdbc:sybase:Tds:" + ip + ":" + port;
        if (sid!=null && sid.length() > 0) {
          url += "/" + sid;
        }
        conn = JDBCUtil.createConnection("com.sybase.jdbc2.jdbc.SybDriver", url, user, passwd);
      } catch (ClassNotFoundException ex) {
        throw ex;
      } catch (SQLException ex) {
        JDBCUtil.close(conn);
        throw new Exception("无法连接到Sybase数据库或连接超时.", ex);
      }
    }
  }
  
  Connection getConnection() {
    if (conn == null) {
      throw new NullPointerException("数据库链接没有初始化,请先初始化.");
    }
    return conn;
  }
  
  /**
   * 获取sybase数据库名称列表
   * @return
   * @throws SQLException
   */
  public List getDbNames() throws Exception {
    List dbNames = new ArrayList();
    Statement stDbName = null;
    ResultSet rsDbName = null;
    try {
      Connection conn = getConnection();
      stDbName = conn.createStatement();
      rsDbName = stDbName.executeQuery(SYBASE_DBS);
      if (rsDbName != null) {
        while (rsDbName.next()) {
          dbNames.add(dbNames.size(), rsDbName.getString("name").trim());
        }
      }
    } catch (SQLException ee) {
      if (logger.isDebugEnabled()) {
        logger.debug("获取数据库名称列表时发生异常．");
      }
      throw new Exception("获取数据库名称列表时发生异常．", ee);
    } finally {
      JDBCUtil.close(rsDbName, stDbName);
    }
    return dbNames;
  }
  
  /**
   * 获取制定数据库实例的数据库实例信息
   * @param dbName 如果为空，则获取当前连接所在数据库的实例信息
   * @return
   * @throws SQLException 
   * @throws SQLException 
   */
  public SybaseDatabase getSybaseDatabase(String dbName) throws Exception {
    Statement stDbs = null;
    ResultSet rsDbs = null;
    Connection conn = getConnection();
    if(conn == null)
      return null;
    try {
      stDbs = conn.createStatement();
    } catch (SQLException e) {
      JDBCUtil.close(stDbs);
      throw e;
    }
    SybaseDatabase db = null;
    ResultSet rsMore = null;
    try {
      stDbs.execute("set quoted_identifier off");
			rsDbs = stDbs.executeQuery(SYBASE_DB_SPACE_USED + dbName);
			if (rsDbs != null) {
				if (rsDbs.next()) {
					db = new SybaseDatabase();
					db.setDbName(rsDbs.getString("name"));
					Double dbsize = convertSizeStrToMbDouble(rsDbs.getString("db_size"));
					db.setDbSize(dbsize);
					boolean hasColumn = false;
					while (stDbs.getMoreResults()) {
						hasColumn = false;
						rsMore = stDbs.getResultSet();
						
						//从结果集中查找是否有free kbytes字段
						ResultSetMetaData rsmd = rsMore.getMetaData();
						int count = rsmd.getColumnCount();
						for (int i = 1; i <= count; i++) {
							if(rsmd.getColumnName(i).equals("free kbytes")){
								hasColumn = true;
								break;
							}
						}
						
						if(!hasColumn)//该结果集中没有free kbytes则continue
							continue;
						
						Double freekbytes = 0D;
						while (rsMore.next()) {
							freekbytes += convertSizeStrToMbDouble(rsMore.getString("free kbytes"));
						}
						db.setUsedSize(dbsize - freekbytes);
						
						break;//取得free kbytes后跳出循环
					}
					
					//判断是否取到了free kbytes
					if(!hasColumn){
						if (logger.isDebugEnabled())
			        logger.debug("获取" + dbName + "的空闲空间出错.");
			      throw new Exception("获取" + dbName + "的空闲空间出错.");
					}
				}
			}
    } catch (SQLException ee) {
      if (logger.isDebugEnabled())
        logger.debug("获取" + dbName + "的信息时发生异常.", ee);
      throw new Exception("获取" + dbName + "的信息时发生异常.", ee);
    } finally {
      JDBCUtil.close(rsMore);
      JDBCUtil.close(rsDbs, stDbs);
    }
    return db;
  }
  
  /**
   * 将数据转化成mb单位的double数据
   * @param str
   * @return
   */
  private double convertSizeStrToMbDouble(String str) {
	  if (str == null)
		  return 0;
    String unit = str.substring(str.length() - 3).trim();
    if(unit.equalsIgnoreCase("MB")){
	  return new Double(str.substring(0, str.length() - 3)).doubleValue();
    }
    double size=0D;
    try{
    	size = new Double(str.trim()).doubleValue();
    }catch(Exception e){
    	
    }
    return  size / 1024d;
    //NumberFormat formatter = NumberFormat.getInstance();
    //formatter.setMaximumFractionDigits(2);
    //return new Double(formatter.format(size)).doubleValue();
  }

  public void close() {
    if (conn != null) {
      JDBCUtil.close(conn);
      conn = null;
    }
  }
}
