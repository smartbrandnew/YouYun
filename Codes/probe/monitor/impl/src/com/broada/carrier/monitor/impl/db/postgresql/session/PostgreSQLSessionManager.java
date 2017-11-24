package com.broada.carrier.monitor.impl.db.postgresql.session;

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

public class PostgreSQLSessionManager {

	private static final Log logger = LogFactory.getLog(DefaultPostgreSQLBaseInfoGetter.class);
	private Connection inConn = null;
	private String ip;
	private String database;
	private int port;
	private String user;
	private String pass;

	public PostgreSQLSessionManager(String ip, String database, int port, String user, String pass) {
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
	
	public List<PostgreSQLSession> getSessionInfo() {
		Statement stmt = null;
		ResultSet rs = null;
		List<PostgreSQLSession> oneLine = new ArrayList<PostgreSQLSession>();
		String sql = "select pid,usename,client_addr,client_port,backend_start from pg_stat_activity";
		try {
			stmt = inConn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				PostgreSQLSession ps = new PostgreSQLSession();
				ps.setProcpid(rs.getString("pid"));
				ps.setUsename(rs.getString("usename"));
				ps.setClient_addr(rs.getString("client_addr"));
				ps.setClient_port(rs.getString("client_port"));
				ps.setBackend_start(rs.getString("backend_start"));
				oneLine.add(ps);
			}
		} catch (SQLException e) {
			logger.error("获取表空间名称出错,SQL=" + sql, e);
			return null;
		} finally {
			JDBCUtil.close(rs, stmt);
		}
		return oneLine;
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
}
