package uyun.bat.datastore.logic.redis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import uyun.bat.datastore.entity.ResourceMetrtics;
import uyun.bat.datastore.util.StringUtils;

public class MetricRedisService {
	@Autowired
	private RedisService redisService;
	private static final String blob_splitter = ";";

	public List<String> getMetricNamesByResId(String resourceId) {
		Jedis jedis = redisService.getJedis();
		List<String> list = jedis.hmget(RedisKeyRule.encodeMetricResKey(resourceId), "metricNames");
		Set<String> set = new HashSet<String>();
		if (list != null) {
			for (String str : list) {
				if (StringUtils.isNotNullAndTrimBlank(str)) {
					String[] array = str.split(blob_splitter);
					set.addAll(new ArrayList<String>(Arrays.asList(array)));
				}
			}
			list.clear();
			list.addAll(set);
		}
		redisService.returnResource(jedis);
		return list;
	}

	public List<String> getMetricNamesByTenantId(String tenantId) {
		Jedis jedis = redisService.getJedis();
		Set<String> set = jedis.smembers(RedisKeyRule.encodeMetricResKey(tenantId));
		List<String> list = new ArrayList<String>();
		list.addAll(set);
		redisService.returnResource(jedis);
		return list;
	}

	/**
	 * 资源上报指标的时候将指标名称插入redis的res和tenant列
	 * @param metricNames
	 * @param resourceId
	 * @param tenantId
     * @return
     */
	public boolean addMetricNames(String[] metricNames, String resourceId, String tenantId) {
		Jedis jedis = redisService.getJedis();
		Pipeline pipeline = jedis.pipelined();
		pipeline.sadd(RedisKeyRule.encodeMetricResKey(tenantId), metricNames);
		Map<String, String> map = new HashMap<String, String>();
		map.put("tenantId", tenantId);
		map.put("metricNames", array2String(metricNames));
		pipeline.hmset(RedisKeyRule.encodeMetricResKey(resourceId), map);
		pipeline.sadd(RedisKeyRule.getMetricNamesAsyncKey(), resourceId);
		pipeline.sync();
		redisService.returnResource(jedis);
		return true;
	}

	/**
	 * 只上报指标的情况将指标名称插入redis的tenant列
	 * @param metricNames
	 * @param resourceId
	 * @param tenantId
	 * @return
	 */
	public boolean addTenantMetricNames(String[] metricNames, String tenantId) {
		Jedis jedis = redisService.getJedis();
		jedis.sadd(RedisKeyRule.encodeMetricResKey(tenantId), metricNames);
		redisService.returnResource(jedis);
		return true;
	}

	public ResourceMetrtics getResourceMetric(String resId) {
		Jedis jedis = redisService.getJedis();
		Map<String, String> map = jedis.hgetAll(RedisKeyRule.encodeMetricResKey(resId));
		String str = map.get("metricNames");
		String[] arrays = string2Array(str);
		String tenantId = map.get("tenantId");
		List<String> metricNames = new ArrayList<String>();
		for (String s : arrays) {
			metricNames.add(s);
		}
		redisService.returnResource(jedis);
		if (metricNames.size() > 0)
			return new ResourceMetrtics(resId, metricNames, tenantId);
		return null;
	}

	private String array2String(String[] array) {
		StringBuilder builder = new StringBuilder();
		for (String str : array) {
			builder.append(str);
			builder.append(blob_splitter);
		}
		String str = builder.toString();
		str = str.substring(0, str.lastIndexOf(blob_splitter));
		return str;
	}

	private String[] string2Array(String str) {
		if (str != null)
			return str.split(blob_splitter);
		return new String[0];
	}

	public boolean deleteMetricNamesByResId(String resId) {
		Jedis jedis = redisService.getJedis();
		long count = jedis.del(RedisKeyRule.encodeMetricResKey(resId));
		redisService.returnResource(jedis);
		return count == 1 ? true : false;
	}

	public long deleteMetricNamesBatch(List<String> resIds) {
		Jedis jedis = redisService.getJedis();
		long count = 0l;
		if (resIds.size() > 0) {
			String[] keys = new String[resIds.size()];
			for (int i = 0; i < keys.length; i++) {
				keys[i] = RedisKeyRule.encodeMetricResKey(resIds.get(i));
			}
			count = jedis.del(keys);
		}
		redisService.returnResource(jedis);
		return count;
	}

	public long deleteMetricResIds(String... ids) {
		Jedis jedis = redisService.getJedis();
		long count = jedis.srem(RedisKeyRule.getMetricNamesAsyncKey(), ids);
		redisService.returnResource(jedis);
		return count;
	}

	public List<String> getAsyncMetricNames() {
		Jedis jedis = redisService.getJedis();
		Set<String> set = jedis.smembers(RedisKeyRule.getMetricNamesAsyncKey());
		List<String> list = new ArrayList<String>();
		list.addAll(set);
		redisService.returnResource(jedis);
		return list;
	}

	/**
	 * 插入单个资源的指标名列表
	 * @param resMetrics
	 * @return
     */
	public boolean addMetricNames(ResourceMetrtics resMetrics) {
		Jedis jedis = redisService.getJedis();
		Map<String, String> map = new HashMap<String, String>();
		map.put("tenantId", resMetrics.getTenantId());
		map.put("metricNames", array2String(resMetrics.getMetricNames().toArray(new String[] {})));
		jedis.hmset(RedisKeyRule.encodeMetricResKey(resMetrics.getResourceId()), map);
		redisService.returnResource(jedis);
		return true;
	}

	/**
	 * 插入租户的指标名列表
	 * @param tenantId
	 * @param names
     * @return
     */
	public boolean addMetricNamesTenantId(String tenantId, String... names) {
		if (names.length > 0) {
			Jedis jedis = redisService.getJedis();
			jedis.sadd(RedisKeyRule.encodeMetricResKey(tenantId), names);
			redisService.returnResource(jedis);
			return true;
		}
		return false;
	}
}
