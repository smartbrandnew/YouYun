package com.broada.carrier.monitor.impl.db.oracle.util;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.common.pool.ResourceHandler;
import com.broada.component.utils.error.ErrorUtil;

/**
 * 连接代理，用于提供连接池特性
 * @author Jiangjw
 */
public class ConnectionProxy implements Connection {
	private static final Log logger = LogFactory.getLog(ConnectionProxy.class);
	private static int ORACLE_CONNECTION_USE_TIME;
	private Connection target;
	private ResourceHandler<ConnectionParameter, ConnectionProxy> handler;	
	private int count;
	private boolean active;
	
	static {
		try {
			ORACLE_CONNECTION_USE_TIME = Integer.parseInt(System.getProperty("oracle.connection.use.time", "5"));
		} catch (Throwable e) {
			ORACLE_CONNECTION_USE_TIME = 5;
		}
	}
	
	void setHandler(ResourceHandler<ConnectionParameter, ConnectionProxy> handler) {
		this.handler = handler;
	}

	public ConnectionProxy(Connection target) {		
		this.target = target;
		this.count = 0;
		this.active = false;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return target.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return target.isWrapperFor(iface);
	}

	@Override
	public Statement createStatement() throws SQLException {
		return target.createStatement();
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return target.prepareStatement(sql);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return target.prepareCall(sql);
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		return target.nativeSQL(sql);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		target.setAutoCommit(autoCommit);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return target.getAutoCommit();
	}

	@Override
	public void commit() throws SQLException {
		target.commit();
	}

	@Override
	public void rollback() throws SQLException {
		target.rollback();
	}

	@Override
	public void close() throws SQLException {
		if (logger.isDebugEnabled())
			logger.debug("连接关闭：" + this);
		if (active) {			
			try {				
				count++;
				if (count >= ORACLE_CONNECTION_USE_TIME)
					handler.destroyResource();
				else
					handler.returnResource();
				active = false;
			} catch (Exception e) {
				throw ErrorUtil.createRuntimeException("归还数据库连接失败", e);			
			}
		}
	}

	@Override
	public boolean isClosed() throws SQLException {
		return target.isClosed();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return target.getMetaData();
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		target.setReadOnly(readOnly);
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return target.isReadOnly();
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		target.setCatalog(catalog);
	}

	@Override
	public String getCatalog() throws SQLException {
		return target.getCatalog();
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		target.setTransactionIsolation(level);
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return target.getTransactionIsolation();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return target.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		target.clearWarnings();
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return target.createStatement(resultSetType, resultSetConcurrency);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return target.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return target.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return target.getTypeMap();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		target.setTypeMap(map);
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		target.setHoldability(holdability);
	}

	@Override
	public int getHoldability() throws SQLException {
		return target.getHoldability();
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return target.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return target.setSavepoint(name);
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		target.rollback(savepoint);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		target.releaseSavepoint(savepoint);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return target.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		return target.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return target.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return target.prepareStatement(sql, autoGeneratedKeys);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return target.prepareStatement(sql, columnIndexes);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return target.prepareStatement(sql, columnNames);
	}

	@Override
	public Clob createClob() throws SQLException {
		return target.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return target.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return target.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return target.createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return target.isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		target.setClientInfo(name, value);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		target.setClientInfo(properties);
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return target.getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return target.getClientInfo();
	}

	@Override
	public String toString() {
		return String.format("%s[id: %d active: %s count: %d %s]", getClass().getSimpleName(), hashCode(), active, count, handler == null ? "" : handler.getKey());
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return target.createArrayOf(typeName, elements);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return target.createStruct(typeName, attributes);
	}

	public void destroy() {
		try {
			target.close();
		} catch (SQLException e) {
			ErrorUtil.warn(logger, "关闭监测连接失败", e);
		}
	}

	public void active() {
		if (logger.isDebugEnabled())
			logger.debug("连接激活：" + this);
		active = true;
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

}
