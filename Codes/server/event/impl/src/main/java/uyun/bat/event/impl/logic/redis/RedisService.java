package uyun.bat.event.impl.logic.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.Set;

public class RedisService {
    private String password;
    private int timeout = 2000;
    private String masterName;
    private String ipList;
    private int database = Protocol.DEFAULT_DATABASE;
    private Set<String> sentinels = new HashSet<String>();
    private JedisSentinelPool pool;

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @SuppressWarnings("unused")
    private void init() {
        // 建立连接池配置参数
        JedisPoolConfig config = new JedisPoolConfig();
        // 设置最大连接数
        config.setMaxTotal(200);
        // 设置最大阻塞时间，记住是毫秒数milliseconds
        config.setMaxWaitMillis(1000);
        // 设置空闲连接
        config.setMaxIdle(30);
        config.setMinIdle(10);
        // 创建连接池
        String[] ipaddrs = ipList.split(",");
        for (String ipaddr : ipaddrs) {
            String[] arrays = ipaddr.split(":");
            sentinels.add(new HostAndPort(arrays[0], Integer.parseInt(arrays[1])).toString());
        }
        pool = new JedisSentinelPool(masterName, sentinels, config, timeout, password, database);

    }

    @SuppressWarnings("unused")
    private void dispose() {
        if (pool != null) {
            pool.close();
            pool.destroy();
        }
    }

    private static RedisService instance = new RedisService();

    public static RedisService getInstance() {
        return instance;
    }

    public JedisSentinelPool getPool() {
        return pool;
    }

    public void setPool(JedisSentinelPool pool) {
        this.pool = pool;
    }

    public Jedis getJedis() {
        return this.pool.getResource();
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

    public Set<String> getSentinels() {
        return sentinels;
    }

    public void setSentinels(Set<String> sentinels) {
        this.sentinels = sentinels;
    }

    public String getIpList() {
        return ipList;
    }

    public void setIpList(String ipList) {
        this.ipList = ipList;
    }

    public int getDatabase() {
			return database;
		}

		public void setDatabase(int database) {
			this.database = database;
		}

		public void treatException(Throwable e) {
        if (logger.isWarnEnabled())
            logger.warn("redis invoke exception:" + e.getMessage());
        if (logger.isDebugEnabled())
            logger.debug("Stack:", e);
    }

    public void returnResource(Jedis jedis) {
        if (jedis != null)
            jedis.close();
    }
}

