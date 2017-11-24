package com.broada.carrier.monitor.method.mysql;

import java.sql.Connection;

import com.broada.carrier.monitor.impl.db.mysql.DefaultMySQLConnection;
import com.broada.carrier.monitor.impl.db.mysql.MySQLConnection;
import com.broada.utils.JDBCUtil;

public class MySQLTester {
	
	public String doTest(String host, Integer port, String encoding, String userName, String pwd) {
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
}
