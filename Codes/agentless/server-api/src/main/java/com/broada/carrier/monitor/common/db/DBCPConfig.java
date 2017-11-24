package com.broada.carrier.monitor.common.db;

import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;

import com.broada.component.utils.lang.SimpleProperties;

/**
 * DBCP数据库连接配置
 */
public class DBCPConfig {	
	private String driver;
	private String url;
	private String user;
	private String password;
	private int initializeSize = 0;
	private int maxActive = 5;
	private int maxIdle = maxActive;
	private int minIdle = 0;
	private int maxWait = 30000;
	private boolean removeAbandoned = true;
	private int removeAbandonedTimeout = 300;
	private boolean logAbandoned = false;

	/**
	 * 从文件中构建配置
	 * @param fileName
	 */
	public DBCPConfig(String fileName) {		
		load(new SimpleProperties(fileName));		
	}
	
	/**
	 * 从Properties中构建配置
	 * @param props
	 */
	public DBCPConfig(Properties props) {
		load(new SimpleProperties(props));
	}

	/**
	 * 使用指定的jdbc常见连接构建配置
	 * @param driver
	 * @param url
	 * @param user
	 * @param password
	 */
	public DBCPConfig(String driver, String url, String user, String password) {		
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.password = password;
	}

	private void load(SimpleProperties props) {
		driver = props.check("jdbc.driverClassName");
		url = props.check("jdbc.url");
		try {
			user = props.check("jdbc.username");
		} catch (Throwable e) {
			user = props.check("jdbc.user");
		}
		password = props.check("jdbc.password");
		initializeSize = props.get("dbcp.initializeSize", initializeSize);
		maxActive = props.get("dbcp.maxActive", maxActive);
		maxIdle = props.get("dbcp.maxIdle", maxIdle);
		minIdle = props.get("dbcp.minIdle", minIdle);
		maxWait = props.get("dbcp.maxWait", maxWait);
		removeAbandoned = props.get("dbcp.removeAbandoned", removeAbandoned);
		removeAbandonedTimeout = props.get("dbcp.removeAbandonedTimeout", removeAbandonedTimeout);
		logAbandoned = props.get("dbcp.logAbandoned", logAbandoned);
	}

	public String getDriver() {
		return driver;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * 连接池建立时，就立即初始化的连接个数，默认为0
	 * @return
	 */
	public int getInitializeSize() {
		return initializeSize;
	}

	/**
	 * 连接池允许建立的最大连接个数，默认为5
	 * @return
	 */
	public int getMaxActive() {
		return maxActive;
	}

	/**
	 * 连接池允许保存的空闲连接最大个数，超过此数量的连接会被释放，默认为maxActive
	 * @return
	 */
	public int getMaxIdle() {
		return maxIdle;
	}

	/**
	 * 连接池允许保存的空闲连接最小个数，当空闲连接小于此数量时，会自动建立，直到满足，默认为0
	 * @return
	 */	
	public int getMinIdle() {
		return minIdle;
	}

	/**
	 * 当连接池没有空闲连接时，请求连接所需要先行的时间，单位ms，默认30s
	 * @return
	 */	
	public int getMaxWait() {
		return maxWait;
	}
	
	/**
	 * 是否启用自动移除无用连接的功能，此属性为了防止连接泄漏
	 * @return
	 */	
	public boolean isRemoveAbandoned() {
		return removeAbandoned;
	}

	/**
	 * 无用连接的超时时间，当一个被申请走的连接，超过此时间未被使用时，会被判断为无用连接，单位s，默认300s
	 * @return
	 */	
	public int getRemoveAbandonedTimeout() {
		return removeAbandonedTimeout;
	}

	/**
	 * 是否记录中断事件
	 * @return
	 */	
	public boolean isLogAbandoned() {
		return logAbandoned;
	}
	
	/**
	 * 根据当前的配置建立一个DBCP数据源
	 * @return
	 */	
	public BasicDataSource createDataSource() {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(driver);
		ds.setUrl(url);
		ds.setUsername(user);
		ds.setPassword(password);
		ds.setInitialSize(initializeSize);
		ds.setMaxActive(maxActive);
		ds.setMaxIdle(maxIdle);
		ds.setMinIdle(minIdle);
		ds.setMaxWait(maxWait);
		return ds;
	}
}
