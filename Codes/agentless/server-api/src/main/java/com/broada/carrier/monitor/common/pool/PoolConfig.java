package com.broada.carrier.monitor.common.pool;

import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;

/**
 * 资源池配置属性类
 */
public class PoolConfig {
	public static final int DEFAULT_MAX_ACTIVE = 1;
	public static final int DEFAULT_MAX_WAIT = 3 * 60 * 1000;
	public static final int DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS = 10 * 60 * 1000;	
	private String poolId;
	private KeyedPoolableObjectFactory objectFactory;
	private int maxActive;
	private long maxWait;
	private long minEvictableIdleTimeMillis;

	public PoolConfig(String poolId, KeyedPoolableObjectFactory objectFactory) {
		this(poolId, objectFactory, 1, 3 * 60 * 1000, 10 * 60 * 1000);
	}

	public PoolConfig(String poolId, KeyedPoolableObjectFactory objectFactory, int maxActive, long maxWait,
			long minEvictableIdleTimeMillis) {
		super();
		this.poolId = poolId;
		this.objectFactory = objectFactory;
		this.maxActive = maxActive;
		this.maxWait = maxWait;
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	/**
	 * 资源池ID
	 * @return
	 */
	public String getPoolId() {
		return poolId;
	}

	/**
	 * 资源池资源工厂，需要提供一个实现此接口的对象
	 * @return
	 */
	public KeyedPoolableObjectFactory getObjectFactory() {
		return objectFactory;
	}

	/**
	 * 资源池最大资源个数，默认为DEFAULT_MAX_ACTIVE
	 * @return
	 */
	public int getMaxActive() {
		return maxActive;
	}

	/**
	 * 如果资源池的资源数已 >= maxActive，则在获取资源时，需要等待本参数指定的时间，单位ms
	 * @return
	 */
	public long getMaxWait() {
		return maxWait;
	}

	/**
	 * 资源池中的资源，如果一直没有人使用，则在多少时间内被销毁，单位ms
	 * @return
	 */
	public long getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}
	
	GenericKeyedObjectPool.Config createConfig() {
		GenericKeyedObjectPool.Config config = new GenericKeyedObjectPool.Config();
		config.maxActive = maxActive;
		config.maxIdle = maxActive;		
		config.maxTotal = Integer.MAX_VALUE;
		config.maxWait = maxWait;
		config.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
		config.timeBetweenEvictionRunsMillis = minEvictableIdleTimeMillis;
		config.testOnBorrow = true;		
		return config;
	}
}
