package uyun.bat.monitor.core.calculate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import uyun.bat.monitor.api.common.util.StateUtil;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.api.mq.MonitorEvent;
import uyun.bat.monitor.core.logic.CheckController;
import uyun.bat.monitor.core.logic.Checker;
import uyun.bat.monitor.core.mq.MQManager;
import uyun.bat.monitor.core.util.MonitorQueryUtil;
import uyun.bat.monitor.core.util.TagUtil;
import uyun.bat.monitor.impl.Startup;
import uyun.bat.monitor.impl.common.RedisConnectionPool;
import uyun.bat.monitor.impl.common.ServiceManager;

public class CalculatorManager {
	private static final Logger logger = LoggerFactory.getLogger(CalculatorManager.class);

	private static final String SEPARATOR = "@@";

	private static CalculatorManager instance = new CalculatorManager() {
	};

	private int metricConsumers = 1;
	private int eventConsumers = 1;
	private int resourceConsumers = 1;
	private int stateMetricConsumers = 1;

	private ExecutorService service;

	private CustomQueue metricQueue;
	private CustomQueue eventQueue;
	private CustomQueue resourceQueue;
	private CustomQueue stateMetricQueue;

	private CalculatorManager() {
	}

	/**
	 * 单个监测器阈值计算时间加锁，单个监测器任务最长时间<br>
	 * 超时未结束，则其他的线程，可能也会执行同个监测器的阈值是否触发判断
	 */
	private static final String LOCK_TIME = "15000";
	/**
	 * 锁所在的redis-key名称
	 */
	private static final String CHECK_HASH_MAP = "bat-monitor:check:hash";

	@SuppressWarnings("unused")
	private void init() {
		Jedis jedis = null;
		try {
			// 清除redis上的过期的bat-monitor:check:hash中的hash数据
			String CLEAN_TIMEOUT_CHECK_SCRIPT = "local results=redis.call('hgetall', KEYS[1]) \n"
					+ "if results ~= 'nil' and #results > 0 then \n" + "for i=1,#results,2 do \n"
					+ "if ((tonumber(ARGV[1]) - tonumber(results[i+1])) > tonumber(ARGV[2])) then \n"
					+ "redis.call('hdel', KEYS[1], results[i]) \n" + "end \n" + "end \n" + "end";

			jedis = RedisConnectionPool.getInstance().getResource();
			List<String> keys = new ArrayList<String>();
			keys.add(CHECK_HASH_MAP);
			List<String> args = new ArrayList<String>();
			args.add(System.currentTimeMillis() + "");
			args.add(LOCK_TIME);
			jedis.eval(CLEAN_TIMEOUT_CHECK_SCRIPT, keys, args);
		} catch (Throwable e) {
			RedisConnectionPool.getInstance().treatException(e, jedis);
		} finally {
			RedisConnectionPool.getInstance().returnResource(jedis);
		}

		service = Executors.newCachedThreadPool();
		metricQueue = new CustomQueue(MonitorType.METRIC.getCode());

		for (int i = 0; i < metricConsumers; i++) {
			service.submit(new Consumer("bat-monitor-calculator-" + MonitorType.METRIC.getCode() + "-" + i, metricQueue));
		}
		eventQueue = new CustomQueue(MonitorType.EVENT.getCode());
		for (int i = 0; i < eventConsumers; i++) {
			service.submit(new Consumer("bat-monitor-calculator-" + MonitorType.EVENT.getCode() + "-" + i, eventQueue));
		}

		resourceQueue = new CustomQueue(MonitorType.HOST.getCode());
		for (int i = 0; i < resourceConsumers; i++) {
			service.submit(new Consumer("bat-monitor-caculator-" + MonitorType.HOST.getCode() + "-" + i, resourceQueue));
		}

		stateMetricQueue = new CustomQueue(MonitorType.APP.getCode());
		for (int i = 0; i < stateMetricConsumers; i++) {
			service.submit(new Consumer("bat-monitor-caculator-" + MonitorType.APP.getCode() + "-" + i, stateMetricQueue));
		}
	}

	public int getStateMetricConsumers() {
		return stateMetricConsumers;
	}

	public void setStateMetricConsumers(int stateMetricConsumers) {
		this.stateMetricConsumers = stateMetricConsumers;
	}

	public int getResourceConsumers() {
		return resourceConsumers;
	}

	public void setResourceConsumers(int resourceConsumers) {
		this.resourceConsumers = resourceConsumers;
	}

	public int getMetricConsumers() {
		return metricConsumers;
	}

	public void setMetricConsumers(int metricConsumers) {
		this.metricConsumers = metricConsumers;
	}

	public int getEventConsumers() {
		return eventConsumers;
	}

	public void setEventConsumers(int eventConsumers) {
		this.eventConsumers = eventConsumers;
	}

	public static CalculatorManager getInstance() {
		return instance;
	}

	public void pushToMetricQueue(String tenantId, String monitorId) {
		metricQueue.push(tenantId + SEPARATOR + monitorId);
	}

	public void pushToEventQueue(String tenantId, String monitorId, String title, String content, boolean recover,
			String resId) {
		StringBuilder sb = new StringBuilder();
		sb.append(tenantId).append(SEPARATOR).append(monitorId).append(SEPARATOR).append(title).append(SEPARATOR)
				.append(content).append(SEPARATOR).append(recover).append(SEPARATOR).append(resId);
		eventQueue.push(sb.toString());
	}

	public void pusthToResourceQueue(String tenantId, String monitorId, String resourceId, short eventSourceType,
			int status, String hostname, String ipaddr) {
		StringBuilder sb = new StringBuilder();
		sb.append(tenantId).append(SEPARATOR).append(monitorId).append(SEPARATOR).append(resourceId).append(SEPARATOR)
				.append(eventSourceType).append(SEPARATOR).append(status).append(SEPARATOR).append(hostname).append(SEPARATOR)
				.append(ipaddr);
		resourceQueue.push(sb.toString());
	}

	public void pusthToStateMetricQueue(String tenantId, String monitorId) {
		stateMetricQueue.push(tenantId + SEPARATOR + monitorId);
	}

	private static final String CHECK_SCRIPT = "local isLocked = redis.call('hexists', KEYS[1], ARGV[1]) \n"
			+ "if isLocked == 0 then \n " + "redis.call('hset', KEYS[1], ARGV[1], ARGV[2]) \n" + "return 1 \n" + "else \n"
			+ "local timestamp = redis.call('hget', KEYS[1], ARGV[1]) \n"
			+ "if ((tonumber(ARGV[2]) - tonumber(timestamp)) > tonumber(ARGV[3])) then \n"
			+ "redis.call('hset', KEYS[1], ARGV[1], ARGV[2]) \n" + "return 1 \n" + "else \n" + "return 0 \n" + "end \n"
			+ "end \n"

	;

	/**
	 * 有其他线程正在进行该监测器的阈值计算，获取检查权限失败
	 */
	private static final int CHECKING = 0;
	/**
	 * 取到进行该监测器的阈值计算的允许
	 */
	private static final int START_TO_CHECK = 1;

	/**
	 * 目标监测器本次未完成阈值计算前，不允许其他线程也执行该监测器的阈值计算<br>
	 * 不然可能会重复发送触发消息通知<br>
	 * 规则: 没线程计算，则添加 ${tenantId}@@${monitorId} ${timestamp}<br>
	 * 前计算节点下线，则判断该${timestamp}与当前时间之差，判断其是否过期15秒
	 */
	private static int checkAndLock(String tenantId, String monitorId) {
		Jedis jedis = null;
		try {
			jedis = RedisConnectionPool.getInstance().getResource();
			List<String> keys = new ArrayList<String>();
			keys.add(CHECK_HASH_MAP);
			List<String> args = new ArrayList<String>();
			args.add(tenantId + SEPARATOR + monitorId);
			args.add(System.currentTimeMillis() + "");
			args.add(LOCK_TIME);
			Object temp = jedis.eval(CHECK_SCRIPT, keys, args);
			if (temp != null && "1".equals(temp.toString())) {
				logger.debug("Other thread processing:" + tenantId + SEPARATOR + monitorId);
				return START_TO_CHECK;
			}
		} catch (Throwable e) {
			RedisConnectionPool.getInstance().treatException(e, jedis);
		} finally {
			RedisConnectionPool.getInstance().returnResource(jedis);
		}
		return CHECKING;
	}

	/**
	 * 按理说15秒够目前的逻辑用，不会出现执行监测器阈值计算超过15秒的情况...<br>
	 * 释放@CalculatorManager.checkAndLock 的锁,以及判断是否需要删除该监测器的相关监测历史
	 */
	private static void releaseLock(String tenantId, String monitorId) {
		Jedis jedis = null;
		try {
			jedis = RedisConnectionPool.getInstance().getResource();
			// 判断是否有变更的监测历史状态需要删除
			String monitorTypeCode = jedis.hget(CHANGE_HASH_MAP, tenantId + SEPARATOR + monitorId);
			if (monitorTypeCode != null && monitorTypeCode.length() > 0) {
				// 按理说15秒够目前的逻辑用，不会出现此处的锁已被其他线程占用的情况...
				// 在有锁的基础上再延长下锁的使用期
				jedis.hset(CHECK_HASH_MAP, tenantId + SEPARATOR + monitorId, System.currentTimeMillis() + "");
				deleteRecord(tenantId, monitorId, MonitorType.checkByCode(monitorTypeCode));
				jedis.hdel(CHANGE_HASH_MAP, tenantId + SEPARATOR + monitorId);
				// 为了事件台异步生效，暂时不删除那个锁,默默等15秒失效
			} else {
				jedis.hdel(CHECK_HASH_MAP, tenantId + SEPARATOR + monitorId);
			}
		} catch (Throwable e) {
			RedisConnectionPool.getInstance().treatException(e, jedis);
		} finally {
			RedisConnectionPool.getInstance().returnResource(jedis);
		}
	}

	private static final String CHANGE_HASH_MAP = "bat-monitor:change:hash";

	/**
	 * 监测器触发条件变更后<br>
	 * 删除旧监测历史
	 */
	public void onMonitorChange(String tenantId, String monitorId, MonitorType monitorType) {
		int result = checkAndLock(tenantId, monitorId);
		if (result == START_TO_CHECK) {
			// redis正常且获取到该锁
			deleteRecord(tenantId, monitorId, monitorType);
			// 监测器修改后的15秒内，暂时不允许计算
		} else {
			if (RedisConnectionPool.getInstance().isConnectionError()) {
				// redis异常
				deleteRecord(tenantId, monitorId, monitorType);
			} else {
				// redis正常，但是有其他的线程正在计算阈值
				// 将该状态存入redis，待那边计算好后再删除就监测记录
				Jedis jedis = null;
				try {
					jedis = RedisConnectionPool.getInstance().getResource();
					jedis.hset(CHANGE_HASH_MAP, tenantId + SEPARATOR + monitorId, monitorType.getCode());
				} catch (Throwable e) {
					RedisConnectionPool.getInstance().treatException(e, jedis);
				} finally {
					RedisConnectionPool.getInstance().returnResource(jedis);
				}
			}
		}
	}

	/**
	 * 监测器变更后,删除监测历史，通知事件服务，相关的事件数据置为已恢复。<br>
	 * 暂时不考虑删除一次，删除失败的情况
	 */
	private static void deleteRecord(String tenantId, String monitorId, MonitorType monitorType) {
		try {
			String[] tags = new String[] { StateUtil.TENANT_ID + TagUtil.SEPARATOR + tenantId,
					StateUtil.MONITOR_ID + TagUtil.SEPARATOR + monitorId };
			ServiceManager.getInstance().getStateService().deleteCheckpoints(StateUtil.generateState(monitorType), tags);
		} catch (Throwable e) {
			// 照顾单元测试找不到外来服务
			if (logger.isWarnEnabled()) {
				logger.warn(e.getMessage());
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Stack:", e);
			}
		}

		try {
			MonitorEvent event = new MonitorEvent(monitorId, tenantId);
			MQManager.getInstance().getMonitorMQService().getMonitorQueueJmsTemplate().convertAndSend(event);
		} catch (Throwable e) {
			if (logger.isWarnEnabled()) {
				logger.warn(e.getMessage());
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Stack:", e);
			}
		}
	}

	private static class Consumer extends Thread {
		private CustomQueue queue;

		private Consumer(String name, CustomQueue queue) {
			this.setName(name);
			this.queue = queue;
		}

		public void run() {
			while (true) {
				String[] ss = null;
				String data = null;
				try {
					data = queue.pop();
					if (data == null) {
						// 消费完了就睡5秒
						Thread.sleep(5000);
						continue;
					}

					// 判断目前是否有其他消费者正在对目标监测器进行阈值校验
					ss = data.split(SEPARATOR);

					int result = checkAndLock(ss[0], ss[1]);
					if (result == CHECKING)
						continue;

					Checker checker = MonitorQueryUtil.getCheckerById(ss);
					if (checker != null)
						CheckController.getInstance().check(checker);
				} catch (Throwable e) {
					logger.warn("monitor process thread execution exception ！ data:[" + data + "] exception:"
							+ e.getMessage());
					if (logger.isDebugEnabled())
						logger.warn("Stack：", e);
				}

				if (ss != null && ss.length > 1)
					releaseLock(ss[0], ss[1]);
			}
		}
	}

	public static void main(String[] args) {
		Startup.getInstance().startup();
		System.out.println(CalculatorManager.checkAndLock("a", "a"));
		System.out.println(CalculatorManager.checkAndLock("a", "a"));
		CalculatorManager.releaseLock("a", "a");
		System.out.println(CalculatorManager.checkAndLock("a", "a"));

	}

}
