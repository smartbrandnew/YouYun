package uyun.bat.event.impl.logic.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import uyun.bat.datastore.api.entity.ResourceModify;
import uyun.bat.datastore.api.entity.ResourceTag;
import uyun.bat.event.api.logic.EventLogic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by lilm on 17-4-13.
 */
public class RedisCustomQueue {
    private static Logger logger = LoggerFactory.getLogger(RedisCustomQueue.class);

    // key1 hashName key2 listName key3 resId|tenantId
    private static final String SCRIPT_PUSH = "if not (redis.call('HGET', KEYS[1], KEYS[3])) then\n"
            + "redis.call('RPUSH', KEYS[2], KEYS[3])\n" + "end\n"
            + "return redis.call('HSET', KEYS[1], KEYS[3], ARGV[1])\n";

    // key1 hashName key2 resId@@tenantId
    private static final String SCRIPT_POP = "local tags = redis.call('HGET', KEYS[1], KEYS[2])\n"
            + "redis.call('HDEL', KEYS[1], KEYS[2])\n"
            + "return tags\n";

    private Object lock = new Object();

    private ExecutorService service = Executors.newCachedThreadPool();
    // 内部消耗消息队列 最长500
    private Queue<ResourceModify> queue = new LinkedBlockingQueue<ResourceModify>();
    // 内存保存队列长度
    private static final Integer maxLength = 500;

    private String type;
    private String hashName;
    private String listName;

    private EventLogic eventLogic;

    public RedisCustomQueue(String type, EventLogic eventLogic) {
        this.type = type;
        this.eventLogic = eventLogic;
        this.hashName = "bat-event-" + type + ":hash";
        this.listName = "bat-event-" + type + ":list";
        // 发布消息到redis线程
        service.submit(new PublishThread());
        // 消费消息线程
        service.submit(new ConsumerThread());
    }

    /**
     * 从mq接收消息放入内存队列
     * @param data
     */
    public void add(ResourceModify data) {
        synchronized (lock) {
            try {
                if (queue.size() >= maxLength) {
                    // 等待队列被线程消费后再继续添加
                    lock.wait();
                    queue.add(data);
                    lock.notifyAll();
                } else {
                    // 将数据添加进队列
                    queue.add(data);
                    lock.notifyAll();
                }
            } catch (InterruptedException e) {
                if (logger.isWarnEnabled())
                    logger.warn("add data exception:", e);
                if (logger.isDebugEnabled())
                    logger.debug("Stack：", e);
            }
        }
    }

    private void push(ResourceModify resModify) {
        Jedis jedis = null;
        try {
            jedis = RedisService.getInstance().getJedis();
            if (jedis != null && resModify != null) {
                executeMsgPush(resModify, jedis);
                return;
            }
        } catch (Throwable e) {
            RedisService.getInstance().treatException(e);
        } finally {
            RedisService.getInstance().returnResource(jedis);
        }
        synchronized (queue) {
            queue.add(resModify);
        }
    }

    /**
     * 执行消息入redis
     * key : resId@@tenantId
     * @param resModify
     * @param jedis
     */
    private void executeMsgPush(ResourceModify resModify, Jedis jedis) {
        if (resModify == null) {
            return;
        }
        String key = resModify.getResourceId() + "@@" + resModify.getTenantId();
        if (CustomType.DEL.getType().equals(type)) {
            jedis.rpush(listName, key);
            return;
        }
        String tags = generateResTags(resModify.getTags());
        List<String> keys = new ArrayList<String>();
        keys.add(hashName);
        keys.add(listName);
        keys.add(key);
        List<String> args = new ArrayList<String>();
        args.add(tags);
        jedis.eval(SCRIPT_PUSH, keys, args);
    }

    private Object pop() {
        Jedis jedis = null;
        try {
            jedis = RedisService.getInstance().getJedis();
            if (jedis != null) {
                if (CustomType.DEL.getType().equals(type)) {
                    return jedis.lpop(listName);
                }
                String key = jedis.lpop(listName);
                if (key != null) {
                    Map<String, Object> data = new HashMap<String, Object>();
                    Object value = jedis.eval(SCRIPT_POP, 2, hashName, key);
                    data.put("key", key);
                    data.put("tags", value != null ? value.toString() : null);
                    return data;
                } else {
                    return null;
                }
            }
        } catch (Throwable e) {
            RedisService.getInstance().treatException(e);
        } finally {
            RedisService.getInstance().returnResource(jedis);
        }
        return null;
    }

    private String generateResTags(List<ResourceTag> tags) {
        if (null == tags || tags.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();
        for (ResourceTag t : tags) {
            sb.append(t.changeToString());
            sb.append(";");
        }
        return sb.substring(0, sb.lastIndexOf(";"));
    }

    private class PublishThread extends Thread {

        private PublishThread() {
            this.setName(type + "-publish");
        }

        public void run() {
            while (true) {
                try {
                    synchronized (lock) {
                        Iterator<ResourceModify> ite = queue.iterator();
                        if (ite.hasNext()) {
                            push(queue.poll());
                            lock.notifyAll();
                        } else {
                            lock.wait();
                        }
                    }
                } catch (Throwable e) {
                    logger.error("Asynchronous message publishing threads execute errors", e);
                }
            }
        }
    }

    private class ConsumerThread extends Thread {


        private ConsumerThread() {
            this.setName(type + "-consumer");
        }

        public void run() {
            while (true) {
                try {
                    Object data = pop();
                    if (data == null) {
                        // 休息5秒
                        Thread.sleep(5000);
                        continue;
                    }
                    if (CustomType.DEL.getType().equals(type)) {
                        if (data instanceof String) {
                            String key = (String) data;
                            //key : resId@@tenantId
                            if (!key.contains("@@"))
                                continue;
                            String[] arr = key.split("@@");
                            if (arr.length > 1) {
                                eventLogic.delete(arr[1], arr[0]);
                            }
                        }
                    } else if (CustomType.UPDATE.getType().equals(type)) {
                        // key : resId@@tenantId
                        // tags: tags1;tags2
                        if (data instanceof Map) {
                            Map<String, Object> map = (Map<String, Object>)data;
                            String key = map.get("key") == null ? null : map.get("key").toString();
                            String tags = map.get("tags") == null ? null : map.get("tags").toString();
                            if (key == null)
                                continue;
                            String[] arr = key.split("@@");
                            if (arr.length > 1)
                                eventLogic.updateEventsByResTags(arr[1], arr[0], tags);
                        }
                    } else {
                        break;
                    }
                } catch (Throwable e) {
                    logger.warn("Asynchronous consumption message execution error！" + e.getMessage());
                    if (logger.isDebugEnabled())
                        logger.warn("Stack：", e);
                }
            }
        }
    }
}
