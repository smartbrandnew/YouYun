package uyun.bat.monitor.impl.common;


import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnectionPoolTest {

	RedisConnectionPool redisCp = RedisConnectionPool.getInstance();
	@Test
	public void test() {
		JedisPoolConfig jedisPoolConfig =new JedisPoolConfig();
		redisCp.setJedisPoolConfig(jedisPoolConfig);
		redisCp.setIpList("ipList");
		redisCp.setMasterName("masterName");
		redisCp.setPassword("password");
		redisCp.setTimeout(1);
		Jedis jedis = new Jedis();
		redisCp.returnResource(jedis);
		System.out.println(redisCp.getJedisPoolConfig()+"\n"+redisCp.getMasterName()
				+"\n"+redisCp.getPassword()+"\n"+redisCp.getTimeout());
	}

}
