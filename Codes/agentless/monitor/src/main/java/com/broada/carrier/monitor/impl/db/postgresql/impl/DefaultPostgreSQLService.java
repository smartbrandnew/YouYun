package com.broada.carrier.monitor.impl.db.postgresql.impl;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import com.broada.carrier.monitor.impl.db.postgresql.PostgreSQLConnection;
import com.broada.carrier.monitor.impl.db.postgresql.PostgreSQLException;
import com.broada.carrier.monitor.impl.db.postgresql.PostgreSQLService;

public class DefaultPostgreSQLService implements PostgreSQLService {

	private Connection inConn = null;
	private String ip;
	private String database;
	private int port;
	private String user;
	private String pass;
	private DefaultPostgreSQLBaseInfoGetter baseGetter;

	public Map<String, String> getBasicInfo() throws PostgreSQLException {
		Map<String, String> sysInfoMap = new HashMap<String, String>();
		sysInfoMap.putAll(baseGetter.getBasicInfo());
		sysInfoMap.put("hostName", ip);
		sysInfoMap.put("dbName", database);
		sysInfoMap.put("port", port + "");
		return sysInfoMap;
	}

	public DefaultPostgreSQLService(String ip, String database, int port, String user, String pass) {
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
			baseGetter = new DefaultPostgreSQLBaseInfoGetter(conn);
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
