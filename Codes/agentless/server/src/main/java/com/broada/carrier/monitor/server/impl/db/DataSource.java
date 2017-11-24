package com.broada.carrier.monitor.server.impl.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.server.impl.config.Config;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.lang.SimpleProperties;

/**
 * 利用dhcp实现的数据连接池
 */
public class DataSource implements javax.sql.DataSource {
	private static final Logger logger = LoggerFactory.getLogger(DataSource.class);
	private static DataSource instance;
	private BasicDataSource ds;

	/**
	 * 用指定的配置文件建立一个数据连接池
	 * @param configFile
	 */
	public DataSource(String configFile) {		
		SimpleProperties props = new SimpleProperties(configFile);		
		ds = new BasicDataSource();
		ds.setDriverClassName(props.get("db.driver", "oracle.jdbc.driver.OracleDriver"));
		ds.setUrl(props.check("jdbc.url"));
		ds.setUsername(props.check("jdbc.username"));
		ds.setPassword(props.check("jdbc.password"));
		ds.setMaxActive(Config.getDefault().getProps().get("db.pool.maxActive", 5));
		ds.setMaxIdle(Config.getDefault().getProps().get("db.pool.maxIdle", ds.getMaxActive()));
		ds.setMinIdle(Config.getDefault().getProps().get("db.pool.minIdle", 1));
	}

	/**
	 * 获取默认实例，默认从系统配置项db.config读取配置文件路径，如果没有提供则使用${user.dir}/conf/jdbc.properties
	 * @return
	 */
	public static DataSource getDefault() {
		if (instance == null) {
			synchronized (DataSource.class) {
				if (instance == null)
					instance = new DataSource(System.getProperty("monitor.db.config", System.getProperty("user.dir")
							+ "/conf/jdbc.properties"));
			}
		}
		return instance;
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
		return false;
	}

	/**
	 * @see javax.sql.DataSource
	 */
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	/**
	 * @see javax.sql.DataSource
	 */
	public Connection getConnection() throws SQLException {
		try {
			return ds.getConnection();
		} catch (SQLException e) {
			ErrorUtil.exit(logger, String.format("数据库连接失败[url: %s user: %s]", ds.getUrl(), ds.getUsername()), e, 1);
			return null;
		}
	}

	/**
	 * @see javax.sql.DataSource
	 */
	public Connection getConnection(String username, String password) throws SQLException {
		return ds.getConnection(username, password);
	}

	/**
	 * 获取连接池
	 * @return
	 */
	public BasicDataSource getDataSource() {
		return ds;
	}
	
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException{
		return null;
	}
}
