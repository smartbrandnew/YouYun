package com.broada.carrier.monitor.method.oracle;

import java.sql.Connection;

import com.broada.carrier.monitor.impl.db.oracle.util.OracleUrlUtil;
import com.broada.utils.JDBCUtil;

public class OracleTester {
	private static final String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";

	public String doTest(String ip, Integer port, String sid, String name, String pwd) {

		Connection connection = null;
		try {
			String url = OracleUrlUtil.getUrl(ip, port, sid, true);
			connection = JDBCUtil.createConnection(DRIVER_NAME, url, name, pwd);
		} catch (Throwable t) {
			return t.getMessage();
		} finally {
			JDBCUtil.close(connection);
		}
		return "true";
	}
}
