package com.broada.carrier.monitor.impl.db.oracle.util;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedPoolableObjectFactory;

import com.broada.carrier.monitor.common.pool.PoolConfig;
import com.broada.carrier.monitor.common.pool.PoolFactory;
import com.broada.carrier.monitor.common.pool.ResourceFactory;
import com.broada.carrier.monitor.common.pool.ResourceHandler;
import com.broada.carrier.monitor.common.pool.ResourceManager;
import com.broada.component.utils.error.ErrorUtil;


public class OracleJDBCUtil {
  private static Log logger = LogFactory.getLog(OracleJDBCUtil.class);
  private static final String POOL_ID = "oracleConnection";
  
  private static final String CLASS_NAME = "oracle.jdbc.driver.OracleDriver";

  private static KeyedObjectPool pool = null;
  
  private static KeyedObjectPool getPool() {
		if (pool == null) {
			synchronized (OracleJDBCUtil.class) {
				if (pool == null) {
					if (!PoolFactory.getDefault().contains(POOL_ID)) {						
						PoolConfig poolConfig = new PoolConfig(POOL_ID, new ConnectionFactory(), 2, PoolConfig.DEFAULT_MAX_WAIT,
								PoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS);
						PoolFactory.getDefault().registry(poolConfig);			
					}
					pool = (KeyedObjectPool) PoolFactory.getDefault().check(POOL_ID);
				}
			}
			
		}
		return pool;
	}
  
  private static class ConnectionFactory implements KeyedPoolableObjectFactory {
		public void activateObject(Object key, Object obj) throws Exception {
			ConnectionProxy conn = (ConnectionProxy) obj;
			conn.active();
			if (logger.isDebugEnabled())
				logger.debug("连接领用：" + obj + " num: " + getPool().getNumActive(key) + " tn: " + getPool().getNumActive());			
		}

		public void destroyObject(Object key, Object obj) throws Exception {
			if (logger.isDebugEnabled())
				logger.debug("连接断开：" + obj);
			ConnectionProxy conn = (ConnectionProxy) obj;
			conn.destroy();			
		}

		public Object makeObject(Object key) throws Exception {
			if (logger.isDebugEnabled())
				logger.debug("连接建立：" + key);			
			return makeConnection((ConnectionParameter) key);						
		}

		public void passivateObject(Object key, Object obj) throws Exception {
			if (logger.isDebugEnabled())
				logger.debug("连接归还：" + obj + " num: " + getPool().getNumActive(key) + " tn: " + getPool().getNumActive());
		}

		public boolean validateObject(Object key, Object obj) {		
			return true;	
		}
	}
  
  private static ResourceManager<ConnectionParameter, ConnectionProxy> manager = new ResourceManager<ConnectionParameter, ConnectionProxy>("OracleMonitorConnections", 10l * 60 * 1000, new ConnectionResourceFactory());
  
  private static class ConnectionResourceFactory implements ResourceFactory<ConnectionParameter, ConnectionProxy> {

		@Override
		public ConnectionProxy borrowResource(ConnectionParameter key) {
	  	try {	  	
	  		return (ConnectionProxy) getPool().borrowObject(key);			
	  	} catch (Throwable e) {
	  		throw ErrorUtil.createRuntimeException("获取数据库连接失败", e);	  		
	  	}	
		}

		@Override
		public void returnResource(ConnectionParameter key, ConnectionProxy resource) {
			try {
				getPool().returnObject(key, resource);
			} catch (Throwable e) {
				ErrorUtil.warn(logger, "归还数据库连接失败", e);
			}
		}

		@Override
		public void destroyResource(ConnectionParameter key, ConnectionProxy resource) {
			try {
				getPool().invalidateObject(key, resource);
			} catch (Throwable e) {
				ErrorUtil.warn(logger, "归还数据库连接失败", e);
			}
		}
  	
  }
  
  /**
   * 创建连接
   * @param url
   * @param user
   * @param password
   * @return
   * @throws ClassNotFoundException 
   * @throws SQLException 
   */
  public static Connection createConnection(String url,String user,String password) throws ClassNotFoundException, SQLException{
  	try {
  		ResourceHandler<ConnectionParameter, ConnectionProxy> handler = manager.borrowResource(new ConnectionParameter(url, user, password));
  		handler.getResource().setHandler(handler);
  		return handler.getResource();
  	} catch (RuntimeException e) {
  		throw new SQLException("获取数据库连接失败", e.getCause());
  	}
  }
  
  private static ConnectionProxy makeConnection(ConnectionParameter param) throws ClassNotFoundException, SQLException{
    Connection con = null;
    try {
      Class.forName(CLASS_NAME);
      Driver d = DriverManager.getDriver(param.getUrl());

      Properties p = new Properties();
      if (param.getUser() != null) {
        p.put("user", param.getUser());
      }
      if (param.getPassword() != null) {
        p.put("password", param.getPassword());
      }
      // 设置读取超时300秒
      p.put("oracle.jdbc.ReadTimeout", "300000");
      // 设置连接超时30秒
      p.put("oracle.net.CONNECT_TIMEOUT", "30000");
      con = d.connect(param.getUrl(), p);
    } catch (ClassNotFoundException e) {
      throw e;
    } catch (SQLException e) {
      throw e;
    }
    return new ConnectionProxy(con);
  }
  
  /**
   * 释放资源,关闭对应的资源对象
   * @param rs
   * @param ps
   * @param conn
   */
  public static void close(ResultSet rs, Statement ps, Connection conn) {
    try {
      if (rs != null) {
        rs.close();
      }
    } catch (Exception e) {
    	ErrorUtil.warn(logger, "关闭resultset失败", e);
    }
    try {
      if (ps != null) {
        ps.close();
      }
    } catch (Exception e) {
    	ErrorUtil.warn(logger, "关闭statement失败", e);
    }
    try {
      if (conn != null && !conn.isClosed()) {
        conn.close();
      }
    } catch (Exception e) {
    	ErrorUtil.warn(logger, "关闭连接失败", e);
    }
  }
  /**
   * 释放资源,关闭对应的资源对象
   * @param rs
   * @param ps
   */
  public static void close(ResultSet rs, Statement ps) {
    close(rs,ps,null);
  }
  /**
   * 释放资源,关闭对应的资源对象
   * @param ps
   * @param conn
   */
  public static void close(Statement ps, Connection conn) {
    close(null,ps,conn);
  }

  /**
   * 释放资源,关闭对应的资源对象
   * @param ps
   */
  public static void close(Statement ps) {
    close(null, ps, null);
  }


  /**
   * 释放资源,关闭对应的资源对象
   * @param rs
   */
  public static void close(ResultSet rs) {
    close(rs, null, null);
  }

  /**
   * 释放资源,关闭对应的资源对象
   * @param conn
   */
  public static void close(Connection conn) {
    close(null, null, conn);
  }
}