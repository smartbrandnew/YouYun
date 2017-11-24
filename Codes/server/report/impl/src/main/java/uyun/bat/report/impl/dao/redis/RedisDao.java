package uyun.bat.report.impl.dao.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Pipeline;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * Created by lilm on 17-3-22.
 */
@Component
public class RedisDao {

    Logger log = LoggerFactory.getLogger(RedisDao.class);

    @Resource
    private RedisConnectionPool redisConnectionPool;

    /**
     * 获取Jedis实例
     * @return Jedis
     */
    public Jedis getJedis() {
        Jedis jedis = null;
        try {
            jedis = redisConnectionPool.getResource();
        } catch (Throwable e) {
            redisConnectionPool.treatException(e, jedis);
        } finally {
            redisConnectionPool.returnResource(jedis);
        }
        return jedis;
    }

    /**
     * 释放jedis资源
     * @param jedis
     */
    public void returnResource(final Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public byte[] hget(byte[] key, byte[] hkey) {
        return getJedis().hget(key, hkey);
    }

    public Long hset(byte[] key, byte[] hkey, byte[] val) {
        return getJedis().hset(key, hkey, val);
    }

    public Long hdel(byte[] key, byte[] hkey) {
        return getJedis().hdel(key, hkey);
    }

    public Set<byte[]> hkeys(byte[] key) {
        return getJedis().hkeys(key);
    }

    public List<String> hmget(String key, String... field) {
        return getJedis().hmget(key, field);
    }

    public Long sadd(String key, String val) {
        return getJedis().sadd(key, val);
    }

    public String spop(String key) {
        return getJedis().spop(key);
    }

    public Long scard(String key) {
        return getJedis().scard(key);
    }

    public String get(String key) {
        return getJedis().get(key);
    }

    public String set(String key, String val) {
        return getJedis().set(key, val);
    }

    public Long del(String key) {
        return getJedis().del(key);
    }

    public String setex(String key, int seconds, String val) {
        return getJedis().setex(key, seconds, val);
    }

    /**
     * 获取redis分布式锁
     * @param key
     * @param val
     */
    public boolean getDistributeLock(String key, String val, int seconds) {
        Jedis j = getJedis();
        Long flag = j.setnx(key, val);
        if (flag != null && flag == 1) {
            j.expire(key, seconds);
            log.info("<---get the disLock success--->");
            return true;
        } else {
            return false;
        }
    }

}
