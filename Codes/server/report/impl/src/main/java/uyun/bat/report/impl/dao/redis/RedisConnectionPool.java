package uyun.bat.report.impl.dao.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.HashSet;
import java.util.Set;

public class RedisConnectionPool {
	private static final Logger logger = LoggerFactory.getLogger(RedisConnectionPool.class);

	private static RedisConnectionPool instance = new RedisConnectionPool();

	public static RedisConnectionPool getInstance() {
		return instance;
	}

	private boolean connectionError = true;
	private ReConnectThread reConnectThread;

	private RedisConnectionPool() {
		reConnectThread = new ReConnectThread();
		reConnectThread.start();
	}

	private JedisPoolConfig jedisPoolConfig;
	private String password;
	private int timeout = 2000;
	private String masterName;
	private String ipList;
	private JedisSentinelPool pool;
	private int database = Protocol.DEFAULT_DATABASE;

	public int getDatabase() {
		return database;
	}

	public void setDatabase(int database) {
		this.database = database;
	}

	private void init() {
		// 创建连接池
		String[] ipaddrs = ipList.split(",");
		Set<String> sentinels = new HashSet<String>();
		for (String ipaddr : ipaddrs) {
			String[] arrays = ipaddr.split(":");
			sentinels.add(new HostAndPort(arrays[0], Integer.parseInt(arrays[1])).toString());
		}
		try {
			pool = new JedisSentinelPool(masterName, sentinels, jedisPoolConfig, timeout, password, database);
			connectionError = false;
		} catch (Exception e) {
			if (logger.isWarnEnabled())
				logger.warn("redis connection pool init failure." + e.getMessage());
			connectionError = true;
		}

	}

	@SuppressWarnings("unused")
	private void dispose() {
		reConnectThread.stop = true;
		if (pool != null) {
			pool.close();
			pool.destroy();
		}
	}

	public JedisPoolConfig getJedisPoolConfig() {
		return jedisPoolConfig;
	}

	public void setJedisPoolConfig(JedisPoolConfig jedisPoolConfig) {
		this.jedisPoolConfig = jedisPoolConfig;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getMasterName() {
		return masterName;
	}

	public void setMasterName(String masterName) {
		this.masterName = masterName;
	}

	public String getIpList() {
		return ipList;
	}

	public void setIpList(String ipList) {
		this.ipList = ipList;
	}

	public Jedis getResource() {
		if (connectionError)
			return null;
		return this.pool.getResource();
	}

	public boolean isConnectionError() {
		return connectionError;
	}

	public void treatException(Throwable e, Jedis jedis) {
		if (e instanceof JedisConnectionException) {
			this.connectionError = true;
		}
		if (logger.isWarnEnabled())
			logger.warn("redis invocation exception:" + e.getMessage());
		if (logger.isDebugEnabled())
			logger.debug("Stack:", e);
	}

	public void returnResource(Jedis jedis) {
		if (jedis != null)
			jedis.close();
	}

	private class ReConnectThread extends Thread {
		private boolean stop = false;

		private ReConnectThread() {
			this.setName("bat-report-redis-ReConnectThread");
		}

		@Override
		public void run() {
			while (!stop) {
				try {
					// 默认每10秒执行一次逻辑判断
					Thread.sleep(10 * 1000);

					if (pool == null) {
						// 未初始化连接池
						init();
					} else {
						// 已初始化连接池，则10秒后尝试连接
						connectionError = false;
					}
				} catch (Throwable e) {
				}
			}
		}
	}

}
