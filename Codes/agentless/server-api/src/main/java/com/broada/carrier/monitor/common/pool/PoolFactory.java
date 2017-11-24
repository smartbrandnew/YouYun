package com.broada.carrier.monitor.common.pool;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;

/**
 * 资源池工厂类
 */
public class PoolFactory {
	private static final Log logger = LogFactory.getLog(PoolFactory.class);
	private static PoolFactory instance;
	private Map<String, KeyedObjectPool> pools = new HashMap<String, KeyedObjectPool>();
	private Map<String, PoolConfig> configs = new HashMap<String, PoolConfig>();

	/**
	 * 获取默认实例，默认实例会从${user.dir}/conf/common.properties读取配置文件，初始化资源池配置
	 * @return
	 */
	public static PoolFactory getDefault() {
		if (instance == null) {
			synchronized (PoolFactory.class) {
				if (instance == null)
					instance = new PoolFactory(System.getProperty("user.dir") + "/conf/common.properties");
			}
		}
		return instance;
	}
	
	public PoolFactory() {		
	}
	
	public PoolFactory(String configFile) {
		try {
			InputStream is = new FileInputStream(configFile);
			Properties props = new Properties();
			props.load(is);
			is.close();
			
			String temp = getConfigItem(props, "ids");
			if (temp != null) {
				String[] ids = temp.split(",");
				for (String id : ids) {
					if (id.trim().length() > 0)
						registry(createConfig(props, id));
				}
			}
			
			logger.debug(String.format("读取配置文件完成，共读取资源池配置[%d]个", configs.size()));
		} catch (Throwable e) {			
			logger.debug(String.format("读取配置文件失败，将使用系统默认配置，文件[%s]，错误：%s", configFile, e));
			logger.debug("堆栈：", e);			
		}
	}

	private PoolConfig createConfig(Properties props, String id) throws Throwable {
		String objectFactoryClassName = checkConfigItem(props, id, "objectFactory");
		Class<?> objectFactoryClass = Class.forName(objectFactoryClassName);
		KeyedPoolableObjectFactory objectFactory = (KeyedPoolableObjectFactory)objectFactoryClass.newInstance();
		PoolConfig result = new PoolConfig(id, objectFactory,
				getConfigItem(props, id, "maxActive", PoolConfig.DEFAULT_MAX_ACTIVE),
				getConfigItem(props, id, "maxWait", PoolConfig.DEFAULT_MAX_WAIT),
				getConfigItem(props, id, "minEvictableIdleTimeMillis", PoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS));	
		return result;
	}
	
	private static String getConfigItem(Properties props, String name) {
		return props.getProperty("numen.pool." + name);
	}
	
	private static int getConfigItem(Properties props, String id, String prop, int defaultValue) {
		String name = "numen.pool." + id + "." + prop;
		String result = props.getProperty(name);
		if (result == null)
			return defaultValue;		
		return Integer.parseInt(result);
	}
	
	private static String checkConfigItem(Properties props, String id, String prop) {
		String name = "numen.pool." + id + "." + prop;
		String result = props.getProperty(name);
		if (result == null)
			throw new IllegalArgumentException(String.format("缺少配置项[%s]", name));
		return result;
	}

	/**
	 * 检查并取出一个资源池，如果指定id的资源池不存在，会弹出异常
	 * @param id
	 * @return
	 */
	public KeyedObjectPool check(String id) {
		KeyedObjectPool result = pools.get(id);
		if (result == null) {
			synchronized (this) {
				result = pools.get(id);
				if (result == null)
					result = create(id);
				pools.put(id, result);
			}
		}
		return result;
	}

	private KeyedObjectPool create(String id) {
		PoolConfig config = checkConfig(id);
		KeyedObjectPool result = new GenericKeyedObjectPool(config.getObjectFactory(), config.createConfig());
		if (logger.isDebugEnabled())
			result = new LoggerKeyedObjectPool(id, result);
		return result;
	}
	
	private PoolConfig checkConfig(String id) {
		PoolConfig result = configs.get(id);
		if (result == null)
			throw new IllegalArgumentException(String.format("资源池配置不存在[id: %s]", id));
		return result;
	}
	
	/**
	 * 是否包含指定ID的资源池
	 * @param id
	 * @return
	 */
	public boolean contains(String id) {
		return configs.containsKey(id);
	}

	/**
	 * 注册一个指定ID的资源池，如果资源池已经存在，则不会作任何事情 
	 * @param config
	 */
	public void registry(PoolConfig config) {
		if (configs.containsKey(config.getPoolId())) {
			logger.warn(String.format("资源池配置已经存在，将不再添加覆盖[id: %s]", config.getPoolId()));
			return;
		}
		configs.put(config.getPoolId(), config);
	}
}
