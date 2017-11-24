package com.broada.carrier.monitor.probe.impl.dispatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.api.client.ProbeUtil;
import com.broada.carrier.monitor.probe.impl.logic.ProbePolicyServiceImpl;
import com.broada.carrier.monitor.probe.impl.logic.ProbeTaskServiceImpl;
import com.broada.carrier.monitor.probe.impl.logic.TaskListener;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.lang.ThreadUtil;
import com.broada.component.utils.pipeline.Pipeline;
import com.broada.component.utils.pipeline.PipelineWorker;
import com.broada.component.utils.pipeline.PipelineWorkerFactory;
import com.broada.component.utils.text.DateUtil;
import com.broada.component.utils.text.Unit;

/**
 * 
 * Probe的监测任务监测调度线程
 * <p>
 * 从原BCC的同名类修改而来,主要修改为调度采集，采集到结果后进行上报处理，如果上报失败则把采集结果缓存起来， 等待网络恢复之后再上报。
 * 
 * 用一个队列保存要监测的服务，监测动作在队列的消费线程里完成 该线程调度时只负责往队列里插入要监测的服务。
 * 
 * @author Maico Pang 2011-08-10
 */

public class MonitorDispatcher {
	private static final Logger logger = LoggerFactory
			.getLogger(MonitorDispatcher.class);
	@Autowired
	private ProbeTaskServiceImpl taskService;
	@Autowired
	private ProbePolicyServiceImpl policyService;
	@Autowired
	private MonitorProcessor taskProcessor;
	/**
	 * 监测队列最大长度
	 */
	private int queueMaxLen = 2000;
	/**
	 * 最大监测线程数
	 */
	private int maxThreadCount = 300;
	/**
	 * 监测超时时间（秒）
	 */
	private int monitorTimeout = 600;
	/**
	 * 监测超时时间（秒）
	 */
	private int monitorInterval = 1;
	/**
	 * 超过此时间则说明监测任务存在延期，分析需要增加监测线程
	 */
	private int monitorDelayMax = 60000;
	/**
	 * 标识线程是否运行
	 */
	private boolean running = false;
	private volatile long executeCount = 0;
	private volatile long delayCount = 0;
	/**
	 * 监测调度队列
	 */
	Pipeline<MonitorTaskItem> monitorPipeline;
	private Map<String, MonitorTaskItem> manualQueue = new ConcurrentHashMap<String, MonitorTaskItem>();
	private MonitorTaskQueue serviceQueue = new MonitorTaskQueue();
	Map<Object, TimeCounter> typeCounter = new HashMap<Object, TimeCounter>();
	Map<Object, TimeCounter> taskCounter = new HashMap<Object, TimeCounter>();
	private TimeCounter globalCounter = new TimeCounter("Global");
	private static MonitorDispatcher instance;
	private boolean chkMonitorsInited = false;
	private List<MonitorTaskItem> chkItems = new ArrayList<MonitorTaskItem>();
	private Map<String, MonitorTaskItem> chkMonitors = new HashMap<String, MonitorTaskItem>();
	private Map<ConcurrencePolicy, PolicyCounter> chkCounters = new HashMap<ConcurrencePolicy, PolicyCounter>();

	/**
	 * 获取默认实例
	 * 
	 * @return
	 */
	public static MonitorDispatcher getDefault() {
		if (instance == null) {
			synchronized (MonitorDispatcher.class) {
				if (instance == null)
					instance = new MonitorDispatcher();
			}
		}
		return instance;
	}

	public void startup() {
		ThreadUtil.createThread(new DispatchThread(), "MonitorDispatchThread")
				.start();
	}

	/**
	 * 监测线程
	 */
	private class DispatchThread implements Runnable {
		public void run() {
			synchronized (serviceQueue) {
				taskService.setListener(new MonitorTaskConcreteListener());
				MonitorTask[] tasks = taskService.getTasks();
				for (MonitorTask task : tasks) {
					logger.info("监测任务信息: {}", task);
					scheduleQueue(task);
				}
			}

			if (monitorPipeline == null) {
				monitorPipeline = new Pipeline<MonitorTaskItem>(
						"MonitorPipeline",
						new PipelineWorkerFactory<MonitorTaskItem>() {
							@Override
							public PipelineWorker<MonitorTaskItem> create() {
								return new MonitorTaskWorker();
							}
						});
				monitorPipeline.setWorkerMaxSize(maxThreadCount);
				monitorPipeline.setQueueMaxSize(queueMaxLen);
				monitorPipeline.startup();
			}

			running = true;
			logger.debug("监测任务调度开始...");
			long interval = monitorInterval * 1000;
			long outputInterval = 5 * 60 * 1000;
			long lastOutputTime = System.currentTimeMillis();
			while (running) {
				try {
					long start = System.currentTimeMillis();
					chkAndMonitorAll();
					long time = System.currentTimeMillis() - start;
					if (time < interval)
						Thread.sleep(interval - time);

					if (logger.isDebugEnabled()) {
						if (start - lastOutputTime > outputInterval) {
							lastOutputTime = start;
							outputMonitorInfo();
						}
					}
				} catch (Throwable ex) {
					logger.error("任务调度出现错误，但调度线程继续运行,错误信息：" + ex.getMessage(),
							ex);
				}
			}
			logger.debug("监测任务调度结束。");
		}
	}

	private MonitorTaskItem createItem(MonitorTask task, Date now) {
		MonitorPolicy policy = policyService.checkPolicy(task.getPolicyCode());
		MonitorRecord record = taskService.getRecord(task.getId());
		if (now == null)
			return new MonitorTaskItem(task, policy, record);
		else
			return new MonitorTaskItem(task, policy, record, now);
	}

	public void scheduleQueue(MonitorTask task) {
		synchronized (serviceQueue) {
			serviceQueue.add(createItem(task, null));
		}
	}

	private void outputMonitorInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("监测调度框架信息输出：\n");
		sb.append("监测次数累计：").append(getMonitorExecuteCount()).append("\n");
		sb.append("监测延误率：").append(getMonitorDelayRate()).append("%\n");
		sb.append("调试队列：队列长度").append(monitorPipeline.getQueueSize())
				.append(" 线程：").append(monitorPipeline.getAliveNum())
				.append(" 僵死线程：").append(monitorPipeline.getDeadNum())
				.append("\n");
		sb.append("所有监测任务：").append(globalCounter).append("\n");
		sb.append("各监测类型耗时排名：\n");

		TimeCounter[] typeCounters = typeCounter.values().toArray(
				new TimeCounter[typeCounter.size()]);
		Arrays.sort(typeCounters);
		for (int i = 0; i < typeCounters.length; i++)
			sb.append(i + 1).append(". ").append(typeCounters[i]).append("\n");

		sb.append("各监测任务耗时排名前15：\n");
		TimeCounter[] taskCounters = taskCounter.values().toArray(
				new TimeCounter[taskCounter.size()]);
		Arrays.sort(taskCounters);
		for (int i = 0; i < taskCounters.length && i < 15; i++)
			sb.append(i + 1).append(". ").append(taskCounters[i]).append("\n");
		logger.debug(sb.toString());
	}

	private boolean canMonitor(MonitorTask task) {
		if (!chkMonitorsInited) {
			chkMonitorsInited = true;
			chkMonitors.clear();
			chkCounters.clear();
			chkItems.clear();
			monitorPipeline.getItems(chkItems);
			for (MonitorTaskItem item : chkItems) {
				chkMonitors.put(item.getTask().getId(), item);
				PolicyCounter counter = checkPolicyCounter(chkCounters, item
						.getTask().getTypeId());
				counter.addRunningMonitorCount(item.getTask());
			}
		}

		if (chkMonitors.containsKey(task.getId())) {
			if (logger.isTraceEnabled())
				logger.trace(String.format("监测任务暂不执行[%s]，原因：已在监测队列或监测中。", task));
			return false;
		}

		PolicyCounter counter = checkPolicyCounter(chkCounters,
				task.getTypeId());
		if (!isPolicyAllow(counter, task)) {
			if (logger.isTraceEnabled())
				logger.trace(String.format("监测任务暂不执行[%s]，原因：策略不允许", task));
			return false;
		}

		counter.addRunningMonitorCount(task);
		return true;
	}

	/**
	 * 查看所有的监测任务 看是否允许监测，并且时间是否已经到 如果条件都满足，那么就监测
	 */
	private void chkAndMonitorAll() {
		Date now = MonitorResultUploader.getDefault().getServerTime();
		if (logger.isTraceEnabled())
			logger.debug("轮询是否有监测任务需要启动，当前服务器时间：" + DateUtil.format(now));
		synchronized (serviceQueue) {
			chkMonitorsInited = false;

			for (MonitorTaskItem item : manualQueue.values()) {
				if (canMonitor(item.getTask())) {
					serviceQueue.remove(item);
					manualQueue.remove(item.getTask().getId());
					monitoringSrv(item, now);
				}
			}

			for (int i = 0; i < serviceQueue.size();) {
				MonitorTaskItem item = serviceQueue.get(i);
				if (item.getNextRunTime() > now.getTime())
					break;

				if (canMonitor(item.getTask())) {
					serviceQueue.remove(item);
					monitoringSrv(item, now);
				} else
					i++;
			}
		}
	}

	private void addManualQueue(MonitorTask task) {
		MonitorTaskItem item = manualQueue.get(task.getId());
		if (item != null)
			return;

		item = createItem(task, MonitorResultUploader.getDefault()
				.getServerTime());
		manualQueue.put(task.getId(), item);

		if (logger.isDebugEnabled())
			logger.debug(String.format("监测任务进入实时队列[队列长度：%d task：%s]",
					manualQueue.size(), task));
	}

	/*
	 * @see com.broada.taskmonitor.MonitorDispatcher1#monitoringSrv(com.broada.
	 * taskmonitor .model.MonitorTask, java.util.Date)
	 */
	private void monitoringSrv(MonitorTaskItem item, Date now) {
		if (item == null || item.getTask() == null)
			return;

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("监测任务进入监测队列[%s lastRunTime: %s]",
					item.getTask(), DateUtil.format(item.getRecord().getTime())));
		}

		monitorPipeline.add(item);
	}

	/*
	 * @see com.broada.taskmonitor.MonitorDispatcher1#stop()
	 */
	public void stop() {
		running = false;
		monitorPipeline.shutdown();
		monitorPipeline = null;
	}

	/**
	 * 暂停
	 */
	public void suspend() {
		monitorPipeline.suspend();
	}

	/**
	 * 继续
	 */
	public void resume() {
		monitorPipeline.resume();
	}

	/**
	 * 是否运行
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return running;
	}

	public int getQueueMaxLen() {
		return queueMaxLen;
	}

	public void setQueueMaxLen(int queueMaxLen) {
		this.queueMaxLen = queueMaxLen;
		if (monitorPipeline != null)
			monitorPipeline.setQueueMaxSize(queueMaxLen);
	}

	public int getMaxThreadCount() {
		return monitorPipeline.getMaxThreadSize();
	}

	public void setMaxThreadCount(int maxThreadCount) {
		this.maxThreadCount = maxThreadCount;
		if (monitorPipeline != null)
			monitorPipeline.setWorkerMaxSize(maxThreadCount);
	}

	public int getMonitorTimeout() {
		return monitorTimeout;
	}

	public void setMonitorTimeout(int monitorTimeout) {
		this.monitorTimeout = monitorTimeout;
		if (monitorPipeline != null)
			monitorPipeline.setWorkerMaxWorkTime(monitorTimeout * 1000);
	}

	public int getMonitorInterval() {
		return monitorInterval;
	}

	public void setMonitorInterval(int monitorInterval) {
		// 为了避免此配置导致监测不及时，目前总是使用1
	}

	public int getMonitorThreadCount() {
		if (monitorPipeline != null) {
			return monitorPipeline.getAliveNum();
		} else {
			return 0;
		}
	}

	public int getQueueSize() {
		if (monitorPipeline != null) {
			return monitorPipeline.getQueueSize();
		} else {
			return 0;
		}
	}

	private static class Counter {
		private int count;

		public Counter(int count) {
			this.count = count;
		}
	}

	private static class PolicyCounter {
		private ConcurrencePolicy policy;
		private Map<String, Counter> countMapByType = new ConcurrentHashMap<String, Counter>();
		private Map<String, Counter> countMapByNode = new ConcurrentHashMap<String, Counter>();

		public PolicyCounter(ConcurrencePolicy policy) {
			super();
			this.policy = policy;
		}

		public void addRunningMonitorCount(MonitorTask task) {
			addRunningMonitorCountByType(task);
			addRunningMonitorCountByNode(task);
		}

		private void addRunningMonitorCountByType(MonitorTask task) {
			String typeId = task.getTypeId();
			Counter value = countMapByType.get(typeId);
			if (value == null) {
				value = new Counter(1);
				countMapByType.put(typeId, value);
			} else {
				synchronized (value) {
					value.count++;
				}
			}
			if (logger.isTraceEnabled())
				logger.trace(String.format(
						"类型监测数量添加[typeId: %s count: %d taskId: %d]", typeId,
						value.count, task.getId()));
		}

		private void addRunningMonitorCountByNode(MonitorTask task) {
			String nodeId = task.getNodeId();
			Counter value = countMapByNode.get(nodeId);
			if (value == null) {
				value = new Counter(1);
				countMapByNode.put(nodeId, value);
			} else {
				synchronized (value) {
					value.count++;
				}
			}
			if (logger.isTraceEnabled())
				logger.trace(String.format(
						"节点监测数量添加[nodeId: %s count: %d taskId: %d]", nodeId,
						value.count, task.getId()));
		}

		private int getRunningMonitorCountByType(String typeId) {
			Counter value = countMapByType.get(typeId);
			if (value == null)
				return 0;
			else
				return value.count;
		}

		private int getRunningMonitorCountByNode(String nodeId) {
			Counter value = countMapByNode.get(nodeId);
			if (value == null)
				return 0;
			else
				return value.count;
		}
	}

	private static PolicyCounter checkPolicyCounter(
			Map<ConcurrencePolicy, PolicyCounter> chkCounters, String typeId) {
		ConcurrencePolicy policy = ConcurrencePolicyFactory.getDefault().check(
				typeId);
		PolicyCounter counter = chkCounters.get(policy);
		if (counter == null) {
			counter = new PolicyCounter(policy);
			chkCounters.put(policy, counter);
		}
		return counter;
	}

	private boolean isPolicyAllow(PolicyCounter counter, MonitorTask task) {
		if (counter.policy.getNodeRunMax() != ConcurrencePolicy.RUN_NO_LIMIT) {
			int count = counter.getRunningMonitorCountByNode(task.getNodeId());
			if (count >= counter.policy.getNodeRunMax()) {
				if (logger.isTraceEnabled())
					logger.trace(String.format(
							"监测任务放弃，任务[%s]，原因：此节点监测任务数[%d]已经达到策略[%s]允许的最大值",
							task, count, counter.policy));
				return false;
			}

			if (logger.isTraceEnabled())
				logger.trace(String.format(
						"监测任务节点策略允许，任务[%s]，原因：此节点监测任务数[%d]还在策略[%s]允许的最大值范围内",
						task, count, counter.policy));
		}
		if (counter.policy.getTypeRunMax() != ConcurrencePolicy.RUN_NO_LIMIT) {
			int count = counter.getRunningMonitorCountByType(task.getTypeId());
			if (count >= counter.policy.getTypeRunMax()) {
				if (logger.isTraceEnabled())
					logger.trace(String.format(
							"监测任务放弃，任务[%s]，原因：此类型监测任务数[%d]已经达到策略[%s]允许的最大值",
							task, count, counter.policy));
				return false;
			}

			if (logger.isTraceEnabled())
				logger.trace(String.format(
						"监测任务类型策略允许，任务[%s]，原因：此类型监测任务数[%d]还在策略[%s]允许的最大值范围内",
						task, count, counter.policy));
		}
		return true;
	}

	static class TimeCounter implements Comparable<TimeCounter> {
		private String name;
		private int count;
		private long time;

		public TimeCounter(String name) {
			super();
			this.name = name;
		}

		public void add(long time) {
			synchronized (this) {
				this.count += 1;
				this.time += time;
			}
		}

		public double getAvgTime() {
			return time * 1.0 / count;
		}

		@Override
		public String toString() {
			return String.format("%s 共%d次，平均耗时%s，吞吐量%.2f个/秒", name, count,
					Unit.ms.formatPrefer(getAvgTime()),
					(count * 1.0 / (time / 1000.0)));
		}

		public int compareTo(TimeCounter o) {
			double thisAvg = this.getAvgTime();
			double anotherAvg = o.getAvgTime();
			if (thisAvg > anotherAvg)
				return -1;
			else if (thisAvg < anotherAvg)
				return 1;
			else
				return 0;
		}
	}

	private static void addMonitorCounter(Map<Object, TimeCounter> map,
			Object name, Object key, MonitorTaskItem item) {
		TimeCounter counter = map.get(key);
		if (counter == null) {
			synchronized (map) {
				counter = map.get(key);
				if (counter == null) {
					counter = new TimeCounter(name.toString());
					map.put(key, counter);
				}
			}
		}

		counter.add(item.getTime());
	}

	private void addMonitorCounter(MonitorTaskItem item) {
		globalCounter.add(item.getTime());
		addMonitorCounter(typeCounter, item.getTask().getTypeId(), item
				.getTask().getTypeId(), item);
		addMonitorCounter(taskCounter, (item.getNode() == null ? "" : item
				.getNode().getIp()) + " " + item.getTask().getName(), item
				.getTask().getId(), item);
	}

	private class MonitorTaskWorker implements PipelineWorker<MonitorTaskItem> {
		public void process(MonitorTaskItem item) {
			long start = System.currentTimeMillis();
			try {
				executeCount++;
				item.start();
				long delay = item.getStartTime() - item.getNextRunTime();
				if (delay < monitorDelayMax) {
					if (logger.isDebugEnabled()) {
						logger.debug(String.format(
								"监测任务开始执行[%s 计划时间: %s 执行延期：%s]",
								item.getTask(), DateUtil.format(new Date(item
										.getNextRunTime())), Unit.ms
										.formatPrefer(delay)));
					}
				} else {
					delayCount++;
					String reason;
					if (monitorPipeline.getQueueSize() > 10)
						reason = "监测器耗时过多或线程不足";
					else
						reason = "此节点上所配置的监测器数量或频率过高";
					logger.warn(String.format(
							"监测任务开始执行，但存在延期[%s 计划时间: %s 执行延期：%s]，原因：%s",
							item.getTask(),
							DateUtil.format(new Date(item.getNextRunTime())),
							Unit.ms.formatPrefer(delay), reason));
				}
				taskProcessor.process(item);
			} finally {
				logger.warn("任务ID:{} \t 类型:{} \t 耗时:{}秒,本次结束运行时间:{}", new Object[]{item.getTask().getId(), item.getTask().getTypeId(), 
						(System.currentTimeMillis() - start)/1000, new Date(System.currentTimeMillis())});
				finish(item);
			}
		}

		private void finish(MonitorTaskItem item) {
			if (item.getEndTime() == 0) {
				item.stop();
				try {
					if (taskService.getTask(item.getTask().getId()) == null) {
						if (logger.isDebugEnabled())
							logger.debug(String.format("监测任务已删除[%s]",
									item.getTask()));
					} else
						scheduleQueue(item.getTask());
				} catch (Throwable e) {
					ErrorUtil.warn(logger, "添加监测任务回到队列失败", e);
				}
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("监测任务结束执行[%s 执行耗时：%s]",
							item.getTask(),
							Unit.ms.formatPrefer(item.getTime())));
				}
				addMonitorCounter(item);
			}
		}

		public void abort(MonitorTaskItem item, Thread thread) {
			finish(item);
		}

		@Override
		public void destroy() {
		}
	}

	public long getMonitorExecuteCount() {
		return executeCount;
	}

	public void unshceduleTask(MonitorTask task) {
		synchronized (serviceQueue) {
			serviceQueue.remove(new MonitorTaskItem(task, null, null));
		}
	}

	public void rescheduleTask(MonitorTask task) {
		synchronized (serviceQueue) {
			unshceduleTask(task);
			scheduleQueue(task);
		}
	}

	public float getMonitorDelayRate() {
		if (executeCount > 0)
			return (float) ((delayCount * 1000 / executeCount) / 10.0);
		else
			return 0;
	}

	private class MonitorTaskConcreteListener implements TaskListener {
		@Override
		public synchronized void onCreated(MonitorTask task) {
			scheduleQueue(task);
		}

		@Override
		public synchronized void onChanged(MonitorTask task) {
			rescheduleTask(task);
		}

		@Override
		public synchronized void onDelete(MonitorTask task) {
			unshceduleTask(task);
		}
	}

	public void dispatchTask(String taskId) {
		addManualQueue(ProbeUtil.checkTask(taskService, taskId));
	}
}
