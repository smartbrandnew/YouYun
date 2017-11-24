package com.broada.carrier.monitor.method.sybase;

import java.sql.Connection;
import java.text.MessageFormat;

import com.broada.utils.JDBCUtil;

public class SybaseTester {
	private static final String DRIVER = "com.sybase.jdbc2.jdbc.SybDriver";

	private static final String URL = "jdbc:sybase:Tds:{0}:{1}";

	public String doTest(String host, SybaseMonitorMethodOption method) {
		Connection connection = null;
		String url = MessageFormat.format(URL, new Object[] { host, String.valueOf(method.getPort()) });
		try {
			connection = JDBCUtil.createConnection(DRIVER, url, method.getUsername(), method.getPassword());
		} catch (Throwable t) {
			return t.toString();
		} finally {
			JDBCUtil.close(connection);
		}
		return "true";
	}
}
