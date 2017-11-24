package com.broada.carrier.monitor.method.jdbc;

import java.sql.Connection;
import java.text.MessageFormat;

import com.broada.carrier.monitor.impl.db.mysql.DefaultMySQLConnection;
import com.broada.carrier.monitor.impl.db.mysql.MySQLConnection;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleUrlUtil;
import com.broada.utils.JDBCUtil;

public class JdbcTester {
	private static final String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";
	private static final String DRIVER_SYBASE = "com.sybase.jdbc2.jdbc.SybDriver";

	private static final String SYBASE_URL = "jdbc:sybase:Tds:{0}:{1}";

	public String doTest(String ip, Integer port, String sid, String name,
			String pwd, String dbType, String encoding) {
		if (dbType.equals("oracle")) {
			return testOracle(ip, port, name, pwd, sid);
		} else if (dbType.equals("mysql")) {
			return testMysql(ip, port, name, pwd, encoding);
		} else if (dbType.equals("sybase")) {
			return testSybase(ip, port, name, pwd);
		} else {
			return "不支持的数据库类型";
		}
		
	}

	private String testMysql(String host, int port, String userName,
			String pwd, String encoding) {
		MySQLConnection mySQLConnection = new DefaultMySQLConnection();
		String url = mySQLConnection.getUrl(host, port, "mysql", encoding);
		Connection connection = null;
		try {
			connection = mySQLConnection.connection(url, userName, pwd);
		} catch (Throwable t) {
			return t.getMessage();
		} finally {
			JDBCUtil.close(connection);
		}
		return "true";
	}

	private String testOracle(String ip, Integer port, String name, String pwd, String sid) {
		// TODO Auto-generated method stub
		Connection connection = null;
		try {
			String url = OracleUrlUtil.getUrl(ip, port, sid, false);
			connection = JDBCUtil.createConnection(DRIVER_NAME, url, name, pwd);
		} catch (Throwable t) {
			return t.getMessage();
		} finally {
			JDBCUtil.close(connection);
		}
		return "true";
	}
	
	public String testSybase(String host, int port, String username, String pwd) {
		Connection connection = null;
		String url = MessageFormat.format(SYBASE_URL, new Object[] { host, String.valueOf(port) });
		try {
			connection = JDBCUtil.createConnection(DRIVER_SYBASE, url, username, pwd);
		} catch (Throwable t) {
			return t.toString();
		} finally {
			JDBCUtil.close(connection);
		}
		return "true";
	}

}
