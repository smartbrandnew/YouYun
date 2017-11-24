package uyun.bat.event.impl.logic.redis;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import uyun.bat.event.api.entity.Event;
import uyun.bat.event.api.entity.EventFault;
import uyun.bat.event.impl.util.EncryptUtil;
import uyun.bat.event.impl.util.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component(value = "eventRedisService")
public class EventRedisService {

    private static final Logger logger = LoggerFactory.getLogger(EventRedisService.class);

    @Autowired
    private RedisService redisService;

    private static final String EVENT_PREFIX="bat-event";

    /**
     * 故障的最后时间小于当前系统时间7天
     * （则删除故障重新建一个故障）
     */
    private static final long FAULT_EXP_TIME=7*24*60*60*1000;

    public EventFault getEventByFaultId(Event event,String faultId){
        String tenantFaultKey = EVENT_PREFIX+":"+event.getTenantId();
        Jedis jedis = redisService.getJedis();
        List<String> keys=new ArrayList<>();
        keys.add(tenantFaultKey);
        List<String> args=new ArrayList<>();
        args.add(EncryptUtil.string2MD5(event.getResId()+event.getIdentity()));
        args.add(event.getServerity()+"");
        args.add(faultId);
        args.add(event.getOccurTime().getTime()+"");
        args.add((null==event.getNow()?System.currentTimeMillis():event.getNow())+"");
        args.add(FAULT_EXP_TIME+"");

        String object = null;
        EventFault fault=new EventFault();
        try{
            String str=jedis.scriptLoad(script);
            if(null!=str||!"".equals(str)){
                object=(String)jedis.evalsha(str,keys,args);
            }else{
                object=(String)jedis.eval(script,keys,args);
            }
        }catch (Throwable e){
            redisService.treatException(e);
        }finally {
            redisService.returnResource(jedis);
        }

        try {
            fault= JsonUtil.decode(object,EventFault.class);
        } catch (IOException e) {
            logger.warn("redis data exception...");
        }
        return fault;
    }

    private String getFaultIdByIdentity(String tenantId,String resId,String identity){
        String tenantFaultKey = EVENT_PREFIX+":"+tenantId;
        identity=EncryptUtil.string2MD5(resId+identity);
        Jedis jedis = redisService.getJedis();
        List<String> list=jedis.hmget(tenantFaultKey,identity);
        redisService.getPool().returnResource(jedis);
        if (null==list||1>list.size()){
            return null;
        }
        return list.get(0);
    }

    public EventFault getByTenantIdAndIdentity(String tenantId,String resId,String identity){
        String faultId=getFaultIdByIdentity(tenantId,resId,identity);
        if (null==faultId){
            return null;
        }
        String tenantFaultKey = EVENT_PREFIX+":"+tenantId;
        Jedis jedis = redisService.getJedis();
        List<String> list=jedis.hmget(tenantFaultKey,"first_time:"+faultId,"relate_count:"+faultId);
        redisService.getPool().returnResource(jedis);
        if (null==list||2!=list.size()){
            return null;
        }
        return  new EventFault(faultId,Long.parseLong(list.get(0)),Long.parseLong(list.get(1)),false);
    }

    public boolean deleteByTenantIdAndIdentity(String tenantId,String resId,String identity){
        String faultId=getFaultIdByIdentity(tenantId,resId,identity);
        identity=EncryptUtil.string2MD5(resId+identity);
        if (null==faultId){
            return false;
        }
        String tenantFaultKey = EVENT_PREFIX+":"+tenantId;
        Jedis jedis = redisService.getJedis();
        Long l=jedis.hdel(tenantFaultKey,identity,"first_time:"+faultId,"relate_count:"+faultId,"last_time:"+faultId);
        redisService.getPool().returnResource(jedis);
        return l>0;
    }

    public boolean deleteByResAndIdentity(String tenantId,String resId,List<String> idens) {
        Jedis jedis =null;
        String tenantFaultKey = EVENT_PREFIX+":"+tenantId;
        try {
            jedis = redisService.getJedis();
            Pipeline pipeline = jedis.pipelined();
            for(String identity:idens){
                String faultId=getFaultIdByIdentity(tenantId,resId,identity);
                identity=EncryptUtil.string2MD5(resId+identity);
                if (null!=faultId){
                    pipeline.hdel(tenantFaultKey,identity,"first_time:"+faultId,"relate_count:"+faultId,"last_time:"+faultId);
                }
            }
            pipeline.sync();
        }catch (Throwable e){
            redisService.treatException(e);
        }finally {
            redisService.returnResource(jedis);
        }
        return true;
    }


    public static final String script="local tenantFaultKey=KEYS[1] \n" +
            "local identity,serverity_str,fault_id,occur_time,current_time,diff_time=ARGV[1],ARGV[2],ARGV[3],ARGV[4],ARGV[5],ARGV[6]  \n" +
            "local serverity=tonumber(serverity_str) \n" +
            "local success=0 \n" +
            "local resultFaultId='' \n" +
            "local firstRelateTime=occur_time \n" +
            "local relateCount=1 \n" +
            "local recover=true \n" +
            "local faultId=redis.call('hget',tenantFaultKey,identity) \n" +
            "if(faultId and string.len(faultId)) then \n" +
            "\tlocal fault=redis.call('hmget',tenantFaultKey,'first_time:'..faultId,'relate_count:'..faultId,'last_time:'..faultId) \n" +
            "\tlocal lastTime=fault[3]\n" +
            "\tif(lastTime and string.len(lastTime) and (tonumber(lastTime)+tonumber(diff_time))<tonumber(current_time)) then\n" +
            "\t\tredis.call('hdel',tenantFaultKey,identity,'first_time:'..faultId,'relate_count:'..faultId,'last_time:'..faultId) \n" +
            "\t\tif(serverity~=success) then \n" +
            "\t\t   recover=false \n" +
            "\t\t  redis.call('hmset',tenantFaultKey,identity,fault_id,'first_time:'..fault_id,occur_time,'last_time:'..fault_id,occur_time,'relate_count:'..fault_id,relateCount) \n" +
            "\t\tend \n" +
            "\t\tresultFaultId = fault_id \n" +
            "\telse \n" +
            "\t\tfirstRelateTime=fault[1] \n" +
            "\t\trelateCount=tonumber(fault[2])+1 \n" +
            "\t\tif(serverity~=success) then \n" +
            "\t\t   recover=false \n" +
            "\t\t\tredis.call('hmset',tenantFaultKey,'relate_count:'..faultId,relateCount,'last_time:'..faultId,occur_time) \n" +
            "\t\telse \n" +
            "\t\t\tredis.call('hdel',tenantFaultKey,identity,'first_time:'..faultId,'relate_count:'..faultId,'last_time:'..faultId) \n" +
            "\t\tend \n" +
            "\t\tresultFaultId = faultId \n" +
            "\tend\n" +
            "else \n" +
            "\tif(serverity~=success) then \n" +
            "\t\t   recover=false \n" +
            "\t\tredis.call('hmset',tenantFaultKey,identity,fault_id,'first_time:'..fault_id,occur_time,'last_time:'..fault_id,occur_time,'relate_count:'..fault_id,relateCount) \n" +
            "\tend \n" +
            "\tresultFaultId = fault_id \n" +
            "end \n" +
            "local result={} \n" +
            "result['faultId']=resultFaultId \n" +
            "result['firstRelateTime']=firstRelateTime \n" +
            "result['relateCount']=relateCount \n" +
            "result['recover']=recover \n"+
            "return cjson.encode(result)";


}
