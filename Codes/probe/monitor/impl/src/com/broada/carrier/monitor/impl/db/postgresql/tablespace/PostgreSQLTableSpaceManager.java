package com.broada.carrier.monitor.impl.db.postgresql.tablespace;

import com.broada.carrier.monitor.impl.db.postgresql.PostgreSQLConnection;
import com.broada.carrier.monitor.impl.db.postgresql.PostgreSQLException;
import com.broada.carrier.monitor.impl.db.postgresql.impl.DefaultPostgreSQLBaseInfoGetter;
import com.broada.carrier.monitor.impl.db.postgresql.impl.DefaultPostgreSQLConnection;
import com.broada.utils.JDBCUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PostgreSQLTableSpaceManager {

	private static final Log logger = LogFactory.getLog(DefaultPostgreSQLBaseInfoGetter.class);
	private Connection inConn = null;
	private String ip;
	private String database;
	private int port;
	private String user;
	private String pass;

	public PostgreSQLTableSpaceManager(String ip, String database, int port, String user, String pass) {
		this.ip = ip;
		this.database = database;
		this.port = port;
		this.user = user;
		this.pass = pass;
	}

	public boolean initConnection() throws PostgreSQLException {
		if (inConn == null) {
			Connection conn = createConnection();
			this.inConn = conn;
			return conn == null ? false : true;
		} else {
			return true;
		}
	}

	private Connection createConnection() throws PostgreSQLException {
		PostgreSQLConnection connection = new DefaultPostgreSQLConnection();
		String url = connection.getUrl(this.ip, this.port, this.database);
		Connection conn = null;
		try {
			conn = connection.connection(url, this.user, this.pass);
			if (conn != null) {
				return conn;
			} else {
				return null;
			}
		} catch (PostgreSQLException ex) {
			throw ex;
		}
	}

	public List<PostgreSQLTableSpace> getTableSpaceInfo() {
		return getTSInfo();
	}

	public void close() {
		try {
			if (inConn != null) {
				inConn.close();
			}
			inConn = null;
		} catch (Exception e) {
		}
	}
	
	public List<PostgreSQLTableSpace> getTSInfo() {
		Statement stmt = null;
	    ResultSet rs = null;
	    String tsName = "";
	    List<PostgreSQLTableSpace> oneLine = new ArrayList<PostgreSQLTableSpace>();
	    String sql ="SELECT spcname FROM pg_tablespace";
	    try {
	        stmt = inConn.createStatement();
	        rs = stmt.executeQuery(sql);
	        while (rs.next()) {
	        	tsName = rs.getString("spcname");
	        	PostgreSQLTableSpace pts = new PostgreSQLTableSpace();
	        	pts.setTsName(tsName);
	        	pts.setTsSize(getTSSize(tsName));
	        	pts.setMaxSize("100");
	        	pts.setIsWacthed(false);
	        	oneLine.add(pts);
	        }
	      } catch (SQLException e) {
	        logger.error("获取表空间名称出错,SQL="+sql,e);
	        return null;
	      } finally {
	        JDBCUtil.close(rs, stmt);
	      }
	      return oneLine;
	}
	
  public double getTSSize(String tsName) {
    Statement stmt = null;
    ResultSet rs = null;
    double tsSize = 0.0d;
    String sql = "select pg_tablespace_size('" + tsName + "');";
    try {
      stmt = inConn.createStatement();
      rs = stmt.executeQuery(sql);
      if (rs.next()) {
        tsSize = rs.getDouble("pg_tablespace_size");
        tsSize = tsSize / 1024 / 1024;
      }
    } catch (SQLException e) {
      logger.error("获取表空间大小出错,SQL=" + sql, e);
    } finally {
      JDBCUtil.close(rs, stmt);
    }
    return tsSize;
  }
}
