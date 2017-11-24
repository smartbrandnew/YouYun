package uyun.bat.datastore.logic.redis;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import uyun.bat.datastore.entity.StateMetricResource;

import java.util.*;

public class StateMetricRedisService {
    @Autowired
    private RedisService redisService;

    private static final String blob_splitter = ";";

    public boolean addMetricNames(String[] metricNames, String resourceId, String tenantId) {
        Jedis jedis =null;
        try {
            jedis = redisService.getJedis();
            Pipeline pipeline = jedis.pipelined();
            pipeline.sadd(RedisKeyRule.encodeStateMetricResKey(tenantId), metricNames);
            Map<String, String> map = new HashMap<String, String>();
            map.put("tenantId", tenantId);
            map.put("metricNames", array2String(metricNames));
            pipeline.hmset(RedisKeyRule.encodeStateMetricResKey(resourceId), map);
            pipeline.sadd(RedisKeyRule.getStateMetricNamesAsyncKey(), resourceId);
            pipeline.sync();
        }catch (Throwable e){
            redisService.treatException(e);
        }finally {
            redisService.returnResource(jedis);
        }
        return true;
    }

    public List<String> getStateAsyncMetricNames() {
        List<String> list = new ArrayList<>();
        Jedis jedis=null;
        try {
            jedis = redisService.getJedis();
            Set<String> set = jedis.smembers(RedisKeyRule.getStateMetricNamesAsyncKey());
             list.addAll(set);
         }catch (Throwable e){
             redisService.treatException(e);
         }finally {
             redisService.returnResource(jedis);
         }
        return list;
    }

    public StateMetricResource getResourceMetric(String resId) {
        Map<String, String> map=new HashMap<>();
        Jedis jedis=null;
        try {
            jedis = redisService.getJedis();
            map = jedis.hgetAll(RedisKeyRule.encodeStateMetricResKey(resId));
        }catch (Throwable e){
            redisService.treatException(e);
        }finally {
            redisService.returnResource(jedis);
        }
        String str = map.get("metricNames");
        String[] arrays = string2Array(str);
        String tenantId = map.get("tenantId");
        List<String> metricNames = new ArrayList<>();
        for (String s : arrays) {
            metricNames.add(s);
        }
        if (metricNames.size() > 0)
            return new StateMetricResource(resId, metricNames, tenantId);
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

    public List<String> getByResId(String resourceId) {
        Map<String, String> map = new HashMap<>();
        Jedis jedis=null;
        try {
            jedis = redisService.getJedis();
            map = jedis.hgetAll(RedisKeyRule.encodeStateMetricResKey(resourceId));
        }catch (Throwable e){
            redisService.treatException(e);
        }finally {
            redisService.returnResource(jedis);
        }
        String str = map.get("metricNames");
        String[] arrays = string2Array(str);
        List<String> metricNames = new ArrayList<String>();
        for (String s : arrays) {
            metricNames.add(s);
        }
        return metricNames;
    }

    public long deleteMetricResIds(String... ids) {
        long count =0;
        Jedis jedis=null;
        try {
            jedis = redisService.getJedis();
            count = jedis.srem(RedisKeyRule.getStateMetricNamesAsyncKey(), ids);
        }catch (Throwable e){
            redisService.treatException(e);
        }finally {
            redisService.returnResource(jedis);
        }
        return count;
    }

    public long deleteByResId(String resId){
        long count=0;
        Jedis jedis=null;
        try {
            jedis=redisService.getJedis();
            jedis.srem(RedisKeyRule.getStateMetricNamesAsyncKey(), resId);
            count=jedis.del(RedisKeyRule.encodeStateMetricResKey(resId));
        }catch (Throwable e){
            redisService.treatException(e);
        }finally {
            redisService.returnResource(jedis);
        }
        return count;
    }

}
