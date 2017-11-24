package com.broada.carrier.monitor.probe.impl.dispatch;

import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;

/**
 * 监测任务优先级队列，按下一次监测时间从小到大排序，弹出队列头的是监测时间最小的监测任务
 * 考虑到执行效率，本类不支持并发，请使用者注意
 * @author Jiangjw
 */
class MonitorTaskQueue {
	private static final Log logger = LogFactory.getLog(MonitorTaskQueue.class);
	private TreeSet<MonitorTaskItem> queue = new TreeSet<MonitorTaskItem>();

	/**
	 * 添加监测任务，对{@link #add(MonitorTaskItem)}的封装
	 * @param srv
	 */
	public void add(MonitorTask task, MonitorPolicy policy, MonitorRecord record) {
		add(new MonitorTaskItem(task, policy, record));
	}

	/**
	 * 获取大小
	 * @return
	 */
	public int size() {
		return queue.size();
	}

	/**
	 * 获取指定位置
	 * @param index
	 * @return
	 */
	public MonitorTaskItem get(int index) {
		logger.debug("Monitor Task队列实时大小: "+size());
		int i = 0;
		for (MonitorTaskItem item : queue) {
			if (i == index)
				return item;		
			i++;
		}
		throw new IllegalArgumentException("索引越界：" + index + " 实时大小：" + queue.size());
	}

	/**
	 * 移除监测任务
	 * @param item
	 */
	public void remove(MonitorTaskItem item) {
		if (queue.remove(item)) {		
			if (logger.isDebugEnabled())
				logger.debug(String.format("监测优先级队列出队[size: %d item: %s]", size(), item));		
		}
	}

	/**
	 * 监测监测任务
	 * @param item
	 */
	public void add(MonitorTaskItem item) {		
		queue.add(item);
		if (logger.isDebugEnabled())
			logger.debug(String.format("监测优先级队列入队[size: %d item: %s]", size(), item));		
	}
}
