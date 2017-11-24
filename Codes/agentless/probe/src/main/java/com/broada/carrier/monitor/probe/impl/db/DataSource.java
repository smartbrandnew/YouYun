package com.broada.carrier.monitor.probe.impl.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Wrapper;

import org.apache.derby.jdbc.ClientDriver;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.common.db.DBCPConfig;
import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.lang.SystemProperties;

public class DataSource implements javax.sql.DataSource {
	private static final Logger logger = LoggerFactory.getLogger(DataSource.class);
	private javax.sql.DataSource ds;
	private DerbyServer server;
	
	public void startup() {		
		String runDir = Config.getWorkDir() + "/data";
		SystemProperties.setIfNotExists("derby.system.home", runDir);
		int port = Config.getDefault().getDatabasePort();		
		Class<?> jdbcDriverClass;
		if (port > 0) {
			server = new DerbyServer(port);
			try {
				server.startup();
			} catch (Exception e) {
				ErrorUtil.exit(logger, "Derby数据库启动失败", e, 1);
			}
			jdbcDriverClass = ClientDriver.class;			
		} else 
			jdbcDriverClass = EmbeddedDriver.class;
		DBCPConfig config = new DBCPConfig(jdbcDriverClass.getName(), "jdbc:derby:probe;create=true", "admin", "broada123");
		this.ds = config.createDataSource();
	}
	
	public void shutdown() {
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (Throwable e) {
			if (e instanceof SQLException && e.getMessage() != null && e.getMessage().contains("Derby system shutdown"))
				return;
			logger.warn(String.format("关闭derby数据库失败。错误：%s", e));
			logger.debug("堆栈：", e);
		}
	}

	/**
	 * @see javax.sql.DataSource
	 */
	public PrintWriter getLogWriter() throws SQLException {
		return ds.getLogWriter();
	}

	/**
	 * @see javax.sql.DataSource
	 */
	public int getLoginTimeout() throws SQLException {
		return ds.getLoginTimeout();
	}

	/**
	 * @see javax.sql.DataSource
	 */
	public void setLogWriter(PrintWriter logWriter) throws SQLException {
		ds.setLogWriter(logWriter);
	}

	/**
	 * @see javax.sql.DataSource
	 */
	public void setLoginTimeout(int loginTimeout) throws SQLException {
		ds.setLoginTimeout(loginTimeout);
	}

	/**
	 * @see javax.sql.DataSource
	 */
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return ((Wrapper) ds).isWrapperFor(iface);
	}

	/**
	 * @see javax.sql.DataSource
	 */
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return ((Wrapper) ds).unwrap(iface);
	}

	/**
	 * @see javax.sql.DataSource
	 */
	public Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

	/**
	 * @see javax.sql.DataSource
	 */
	public Connection getConnection(String username, String password) throws SQLException {
		return ds.getConnection(username, password);
	}

	public javax.sql.DataSource getDataSource() {
		return ds;
	}
	
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException{
		return null;
	}
}
