package uyun.bat.monitor.core.mq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerManager {
	private static final Logger logger = LoggerFactory.getLogger(ConsumerManager.class);

	/**
	 * 内存最多保存数据个数
	 */
	private int maxLength = 500;

	/**
	 * Map中的数据个数
	 */
	private int count = 0;

	private ExecutorService service;

	/**
	 * 租户id队列，保持队形
	 */
	private LinkedHashSet<String> queue;

	/**
	 * <code>map</code>
	 */
	private Map<String, List<MQData>> map;

	private Object lock = new Object();

	private Consumer consumer;

	protected ConsumerManager(String type, Consumer consumer, int consumersCount, int maxLength) {
		this.consumer = consumer;
		this.queue = new LinkedHashSet<String>();
		this.map = new HashMap<String, List<MQData>>();
		this.maxLength = maxLength;
		this.service = Executors.newCachedThreadPool();

		for (int i = 0; i < consumersCount; i++) {
			service.submit(new ConsumerThread("bat-monitor-mqconsumer-" + type + "-" + i));
		}
	}

	public void push(MQData data) {
		synchronized (lock) {
			try {
				if (count <= 0) {
					addData(data);
					lock.notifyAll();
				} else {
					if (queue.size() >= maxLength) {
						// 等待队列被线程消费后再继续添加
						lock.wait();
						addData(data);
						lock.notifyAll();
					} else {
						// 将数据添加进队列
						addData(data);
						lock.notifyAll();
					}
				}
			} catch (Throwable e) {
				if (logger.isWarnEnabled())
					logger.warn("add data exception:", e);
				if (logger.isDebugEnabled())
					logger.debug("Stack：", e);
			}
		}
	}

	private void addData(MQData data) {
		queue.add(data.getTenantId());

		List<MQData> list = map.get(data.getTenantId());
		if (list == null) {
			list = new ArrayList<MQData>();
			map.put(data.getTenantId(), list);
		}
		list.add(data);
		count++;
	}

	private class ConsumerThread extends Thread {

		private ConsumerThread(String name) {
			this.setName(name);
		}

		public void run() {
			while (true) {
				try {
					String tenantId = null;
					List<MQData> data = null;
					synchronized (lock) {
						Iterator<String> ite = queue.iterator();
						if (ite.hasNext()) {
							tenantId = ite.next();
							data = map.remove(tenantId);
							count -= data.size();
							queue.remove(tenantId);
							lock.notifyAll();
						} else {
							lock.wait();
						}
					}
					if (data != null && !data.isEmpty())
						consumer.doConsume(tenantId, data);
				} catch (Throwable e) {
					logger.error("asynchronous consumer thread execution error", e);
				}
			}
		}
	}
}
