package uyun.bat.monitor.core.calculate;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import redis.clients.jedis.Jedis;
import uyun.bat.monitor.impl.Startup;
import uyun.bat.monitor.impl.common.RedisConnectionPool;

/**
 * 全局的redis队列
 */
class CustomQueue {
	private static final String SCRIPT_PUSH = "if redis.call('SADD', KEYS[1], KEYS[3]) ~= 0 then\n"
			+ "return redis.call('RPUSH', KEYS[2], KEYS[3])\n" + "end\n" + "return 0\n";

	private static final String SCRIPT_POP = "local monitorData = redis.call('LPOP', KEYS[1])\n"
			+ "if monitorData ~= nil then\n" + "redis.call('SREM', KEYS[2], monitorData)\n" + "end\n"
			+ "return monitorData\n";

	private static Set<String> queue = new LinkedHashSet<String>();

	private String name;
	private String listName;
	private String setName;

	protected CustomQueue(String name) {
		this.name = name;
		listName = "bat-monitor:" + name + ":list";
		setName = "bat-monitor:" + name + ":set";
	}

	protected String getName() {
		return name;
	}

	protected void push(String id) {
		Jedis jedis = null;
		try {
			jedis = RedisConnectionPool.getInstance().getResource();
			if (jedis != null) {
				jedis.eval(SCRIPT_PUSH, 3, setName, listName, id);
				if (!queue.isEmpty()) {
					synchronized (queue) {
						Iterator<String> ite = queue.iterator();
						while (ite.hasNext()) {
							jedis.eval(SCRIPT_PUSH, 3, setName, listName, ite.next());
						}
					}
				}
				return;
			}
		} catch (Throwable e) {
			RedisConnectionPool.getInstance().treatException(e, jedis);
		} finally {
			RedisConnectionPool.getInstance().returnResource(jedis);
		}

		// 其他异常先添加进本地队列
		synchronized (queue) {
			queue.add(id);
		}
	}

	protected String pop() {
		Jedis jedis = null;
		try {
			jedis = RedisConnectionPool.getInstance().getResource();
			if (jedis != null) {
				Object value = jedis.eval(SCRIPT_POP, 2, listName, setName);
				return value != null ? value.toString() : null;
			}
		} catch (Throwable e) {
			RedisConnectionPool.getInstance().treatException(e, jedis);
		} finally {
			RedisConnectionPool.getInstance().returnResource(jedis);
		}

		synchronized (queue) {
			Iterator<String> ite = queue.iterator();
			if (ite.hasNext()) {
				String temp = ite.next();
				queue.remove(temp);
				return temp;
			} else {
				return null;
			}
		}
	}

	public static void main(String[] args) {
		Startup.getInstance().startup();
		for (int i = 0; i < 100000; i++) {
			try {
				Thread.currentThread().sleep(300);
			} catch (InterruptedException e) {

			}
			CalculatorManager.getInstance().pushToMetricQueue("e0a67e986a594a61b3d1e523a0a39c77",
					"10000000000000000000000000" + (200000 - i));
		}
	}
}
