package com.broada.carrier.monitor.method.postgresql;

import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.db.postgresql.PostgreSQLConnection;
import com.broada.carrier.monitor.impl.db.postgresql.impl.DefaultPostgreSQLConnection;
import com.broada.utils.JDBCUtil;

public class PostgreSQLTester {
	private static final Log logger = LogFactory.getLog(PostgreSQLTester.class);
	public String doTest(String host, Integer port, String databaseName, String userName, String pwd) {
		PostgreSQLConnection postgreSQLConnection = new DefaultPostgreSQLConnection();
		String url = postgreSQLConnection.getUrl(host, port, databaseName);
		Connection connection = null;
		try {
			connection = postgreSQLConnection.connection(url, userName, pwd);
		} catch (Throwable t) {
			logger.warn(String.format("建立数据库连接失败失败。错误：%s", t));
			logger.debug("堆栈：", t);
			return String.format("建立数据库连接失败，请确定数据库连接参数是否准确，并且数据库服务已启动。");			
		} finally {
			JDBCUtil.close(connection);
		}
		return "true";
	}
}
