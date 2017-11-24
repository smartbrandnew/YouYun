package uyun.bat.datastore.logic.redis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceCount;

public class ResourceRedisService {
	@Autowired
	private RedisService redisService;

	public boolean insert(Resource resource, long lastCount) {
		Jedis jedis = redisService.getJedis();
		try {
			Map<String, String> map = RedisDataModelUtil.generateResourceRedisHash(resource);
			String id = RedisKeyRule.encodeResKey(resource.getId());
			jedis.hmset(id, map);
			//增加计数
			jedis.set(RedisKeyRule.encodeResCountKey(resource.getTenantId()), Long.toString(lastCount + 1));
			//待同步队列
			jedis.sadd(RedisKeyRule.getResAsyncKey(), resource.getId());
			return true;
		} catch (Throwable e) {
			redisService.treatException(e);
		} finally {
			redisService.returnResource(jedis);
		}
		return false;
	}

	public boolean update(Resource resource) {
		Jedis jedis = redisService.getJedis();
		try {
			Map<String, String> map = RedisDataModelUtil.generateResourceRedisHash(resource);
			String id = RedisKeyRule.encodeResKey(resource.getId());
			jedis.hmset(id, map);
			jedis.sadd(RedisKeyRule.getResAsyncKey(), resource.getId());
			return true;
		} catch (Throwable e) {
			redisService.treatException(e);
		} finally {
			redisService.returnResource(jedis);
		}
		return false;
	}

	public boolean delete(String resourceId) {
		Jedis jedis = redisService.getJedis();
		jedis.del(RedisKeyRule.encodeResKey(resourceId));
		redisService.returnResource(jedis);
		return true;
	}

	public long insert(List<Resource> resources, String tenantId, long lastCount) {
		Jedis jedis = redisService.getJedis();
		Pipeline pipeline = jedis.pipelined();
		for (Resource resource : resources) {
			Map<String, String> attritutes = RedisDataModelUtil.generateResourceRedisHash(resource);
			String id = RedisKeyRule.encodeResKey(resource.getId());
			pipeline.hmset(id, attritutes);
			pipeline.sadd(RedisKeyRule.getResAsyncKey(), resource.getId());
		}
		//增加计数
		pipeline.set(RedisKeyRule.encodeResCountKey(tenantId), Long.toString(lastCount + resources.size()));
		pipeline.sync();
		redisService.returnResource(jedis);
		return resources.size();
	}

	public Resource queryResById(String id) {
		Jedis jedis = redisService.getJedis();
		Map<String, String> attritutes = jedis.hgetAll(RedisKeyRule.encodeResKey(id));
		redisService.returnResource(jedis);
		return RedisDataModelUtil.generateResource(id, attritutes);
	}

	public long deleteAuthorizationRes(List<String> ids) {
		Jedis jedis = redisService.getJedis();
		long count = 0l;
		if (ids.size() > 0) {
			String[] keys = new String[ids.size()];
			for (int i = 0; i < keys.length; i++) {
				keys[i] = RedisKeyRule.encodeResKey(ids.get(i));
			}
			count = jedis.del(keys);
		}
		redisService.returnResource(jedis);
		return count;
	}

	public long insertResCountBatch(List<ResourceCount> list) {
		Jedis jedis = redisService.getJedis();
		Pipeline pipeline = jedis.pipelined();
		for (ResourceCount resCount : list) {
			pipeline.setnx(RedisKeyRule.encodeResCountKey(resCount.getTenantId()), resCount.getCount());
		}
		pipeline.sync();
		redisService.returnResource(jedis);
		return list.size();
	}

	public int getResCountByTenantId(String tenantId) {
		Jedis jedis = redisService.getJedis();
		String val = jedis.get(RedisKeyRule.encodeResCountKey(tenantId));
		redisService.returnResource(jedis);
		if (val != null)
			return Integer.parseInt(val);
		return 0;
	}

	public boolean insertResCount(String tenantId, String count) {
		Jedis jedis = redisService.getJedis();
		String message = jedis.set(RedisKeyRule.encodeResCountKey(tenantId), count);
		redisService.returnResource(jedis);
		if ("OK".equalsIgnoreCase(message))
			return true;
		return false;

	}

	public List<String> getAsyncResIds() {
		Jedis jedis = redisService.getJedis();
		Set<String> set = jedis.smembers(RedisKeyRule.getResAsyncKey());
		List<String> list = new ArrayList<String>();
		list.addAll(set);
		redisService.returnResource(jedis);
		return list;
	}

	public long delAsyncResIds(String[] ids) {
		Jedis jedis = redisService.getJedis();
		long count = jedis.srem(RedisKeyRule.getResAsyncKey(), ids);
		redisService.returnResource(jedis);
		return count;
	}

	private static final String GET_ONLINE_RESOURCES_FROM_REDIS = "local array={} \n"
			+ "if redis.call('EXISTS',KEYS[1]) ~= 0 then\n" + "local result = redis.call('SMEMBERS',KEYS[1])\n"
			+ "if(#result > 1) then\n" + "for i=1,#result do\n" + "if result[i] ~= nil then\n"
			+ "local lastCollectTime = tonumber(redis.call('HGET',ARGV[1]..result[i],'lastCollectTime'))\n"
			+ "if (lastCollectTime ~= nil and tonumber(ARGV[2]) <= lastCollectTime) then\n"
			+ "array[(#array+1)]=result[i]\n" + "end\n" + "end\n" + "end\n" + "end\n" + "end\n" + "return array";
	
	/**
	 * 查询该时间点前有上报过数据的资源Id
	 */
	public List<String> queryOnlineResource(long lastCollectTime) {
		Jedis jedis = null;
		try {
			jedis = redisService.getJedis();
			Object temp = jedis.eval(GET_ONLINE_RESOURCES_FROM_REDIS,
					Arrays.asList(new String[] { RedisKeyRule.getResAsyncKey() }),
					Arrays.asList(new String[] { RedisKeyRule.resource_prefix, lastCollectTime + "" }));
			if (temp != null) {
				List<String> datas = (List<String>) temp;
				if (!datas.isEmpty())
					return datas;
			}
		} catch (Throwable e) {
			redisService.treatException(e);
		} finally {
			redisService.returnResource(jedis);
		}
		return new ArrayList<String>();
	}
}
