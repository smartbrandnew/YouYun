package com.broada.carrier.monitor.impl.db.mysql.impl; /**
 * ********************************************************************************************************************
 * Module: DefaultMySQLService.java Author: zcy Purpose: Defines the Class DefaultMySQLService
 * ********************************************************************************************************************
 */

import com.broada.carrier.monitor.impl.db.mysql.*;
import com.broada.carrier.monitor.method.mysql.MySQLMonitorMethodOption;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zhoucy(zhoucy@broada.com.cn)
 */
public class DefaultMySQLService implements MySQLService {
	private MySQLTableInfoRetriever tableInfoRetriever = null;

	private MySQLDatabaseInfoRetriever databaseInfoRetriever = null;

	private MySQLMonitorMethodOption method = null;

	private String host;

	/**
	 * 内部维持的链接
	 */
	private Connection inConn = null;

	private void setConnection(Connection connection) {
		this.inConn = connection;

		tableInfoRetriever = new DefaultMySQLTableInfoRetriever(inConn);

		databaseInfoRetriever = new DefaultMySQLDatabaseInfoRetriever(inConn);
	}

	public DefaultMySQLService(String host, MySQLMonitorMethodOption method) {
		if (method == null) {
			throw new NullPointerException("MySql访问配置参数不能为空.");
		}
		this.host = host;
		this.method = method;
	}

	public Map getAllStatus() {
		return databaseInfoRetriever.getAllStatus();
	}

	public Map getAllVariable() {
		return databaseInfoRetriever.getAllVariable();
	}

	public String getStatus(String statusName) {
		return databaseInfoRetriever.getStatus(statusName);
	}

	public String getVariable(String varName) {
		return databaseInfoRetriever.getVariable(varName);
	}

	public List showDatabases() {
		return databaseInfoRetriever.showDatabases();
	}

	/*
	 *
	 * @see com.broada.srvmonitor.impl.mysql.MySQLService#initConnection()
	 */
	public boolean initConnection() throws MySQLException {
		if (inConn == null) {
			Connection conn = createConnection(host, method);
			setConnection(conn);
			return conn == null ? false : true;
		} else {
			return true;
		}
	}

	/**
	 * 内部初始化数据库
	 *
	 * @param host
	 * @param method
	 * @return
	 * @throws MySQLException
	 */
	private static Connection createConnection(String host, MySQLMonitorMethodOption method) throws MySQLException {
		MySQLConnection mySQLConnection = new DefaultMySQLConnection();
		String url = mySQLConnection.getUrl(host, method.getPort(), method.getDatabase(), method.getEncoding());
		Connection conn = null;
		try {
			conn = mySQLConnection.connection(url, method.getUsername(), method.getPassword());
			if (conn != null) {
				return conn;
			} else {
				return null;
			}
		} catch (MySQLException ex) {
			throw ex;
		}
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

	public Map<String, Long> getAllDatabaseSize() {
		Map<String, Long> dbSizeMap = new HashMap<String, Long>();
		List databases = databaseInfoRetriever.showDatabases();
		if (databases == null)
			return dbSizeMap;
		for (int index = 0; index < databases.size(); index++) {
			String dbName = (String) databases.get(index);

			List tableStatusList = tableInfoRetriever.getTableStatusInfos(dbName);
			dbSizeMap.put(dbName, getDatabaseSize(tableStatusList));
		}
		return dbSizeMap;
	}

	private Long getDatabaseSize(List tableStatusList) {
		long size = 0L;
		if (tableStatusList == null)
			return new Long(0);
		for (int index = 0; index < tableStatusList.size(); index++) {
			TableStatus tableStatus = (TableStatus) tableStatusList.get(index);

			size += tableStatus.getDataLength() + tableStatus.getIndexLength();
		}
		return new Long(size);
	}

	public void setHost(String host) {
		this.host = host;
	}
}