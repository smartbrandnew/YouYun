package com.broada.carrier.monitor.probe.impl.dispatch;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.impl.entity.ProbeSideMonitorContext;
import com.broada.carrier.monitor.probe.impl.logic.ProbeTaskServiceImpl;
import com.broada.carrier.monitor.probe.impl.logic.ProbeTypeServiceImpl;
import com.broada.carrier.monitor.server.api.entity.CollectParams;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.CollectTaskSign;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.TestParams;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.pipeline.Pipeline;
import com.broada.component.utils.pipeline.PipelineWorker;
import com.broada.component.utils.pipeline.PipelineWorkerFactory;
import com.broada.component.utils.text.Unit;

/**
 * 
 * Probe的工作任务监测调度线程
 * <p>
 * 从原BCC的同名类修改而来,主要修改为调度采集，采集到结果后进行上报处理，如果上报失败则把采集结果缓存起来， 等待网络恢复之后再上报。
 * 
 * 用一个队列保存要监测的服务，监测动作在队列的消费线程里完成 该线程调度时只负责往队列里插入要监测的服务。
 * 
 * @author Maico Pang 2011-08-10
 */

public class WorkDispatcher {
	private static final Logger logger = LoggerFactory.getLogger(WorkDispatcher.class);
	@Autowired
	private ProbeTaskServiceImpl taskService;
//	@Autowired
//	private ProbePolicyServiceImpl policyService;
	@Autowired
	private ProbeTypeServiceImpl typeService;
	private static Map<String, CollectResult> collectResultMap = new HashMap<String, CollectResult>();
	private static Map<String, WorkItem> workItemMap = new HashMap<String, WorkItem>();
	/**
	 */
	private int queueMaxLen = 200;
	/**
	 * 最大监测线程数
	 */
	private int maxThreadCount = 20;
	/**
	 * 监测超时时间（秒）
	 */
	private int monitorTimeout = 600;
	/**
	 * 监测超时时间（秒）
	 */
	private int monitorInterval = 1;
	/**
	 * 标识线程是否运行
	 */
	private boolean running = false;
	private volatile long executeCount = 0;
	/**
	 * 监测调度队列
	 */
	private Pipeline<WorkItem> workPipeline;
	private static WorkDispatcher instance;

	/**
	 * 获取默认实例
	 * 
	 * @return
	 */

	public static WorkDispatcher getDefault() {
		if (instance == null) {
			synchronized (WorkDispatcher.class) {
				if (instance == null) {
					instance = new WorkDispatcher();
					instance.startup();
				}
			}
		}
		return instance;
	}

	public void startup() {
		if (workPipeline == null) {
			workPipeline = new Pipeline<WorkItem>("WorkPipeline", new PipelineWorkerFactory<WorkItem>() {
				@Override
				public PipelineWorker<WorkItem> create() {
					return new WorkRunner();
				}
			});
			workPipeline.setWorkerMaxSize(maxThreadCount);
			workPipeline.setQueueMaxSize(queueMaxLen);
			workPipeline.startup();

		}
	}

	/*
	 * @see com.broada.taskmonitor.MonitorDispatcher1#stop()
	 */
	public void stop() {
		running = false;
		workPipeline.shutdown();
		workPipeline = null;
	}

	/**
	 * 暂停
	 */
	public void suspend() {
		workPipeline.suspend();
	}

	/**
	 * 继续
	 */
	public void resume() {
		workPipeline.resume();
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
		if (workPipeline != null)
			workPipeline.setQueueMaxSize(queueMaxLen);
	}

	public int getMaxThreadCount() {
		return workPipeline.getMaxThreadSize();
	}

	public void setMaxThreadCount(int maxThreadCount) {
		this.maxThreadCount = maxThreadCount;
		if (workPipeline != null)
			workPipeline.setWorkerMaxSize(maxThreadCount);
	}

	public int getMonitorTimeout() {
		return monitorTimeout;
	}

	public void setMonitorTimeout(int monitorTimeout) {
		this.monitorTimeout = monitorTimeout;
		if (workPipeline != null)
			workPipeline.setWorkerMaxWorkTime(monitorTimeout * 1000);
	}

	public int getMonitorInterval() {
		return monitorInterval;
	}

	public void setMonitorInterval(int monitorInterval) {
		// 为了避免此配置导致监测不及时，目前总是使用1
	}

	public int getMonitorThreadCount() {
		if (workPipeline != null) {
			return workPipeline.getAliveNum();
		} else {
			return 0;
		}
	}

	public int getQueueSize() {
		if (workPipeline != null) {
			return workPipeline.getQueueSize();
		} else {
			return 0;
		}
	}

	private class WorkRunner implements PipelineWorker<WorkItem> {

		public void process(WorkItem item) {
			try {
				executeCount++;
				item.start();

				Object output;
				switch (item.getType()) {
				case COLLECT:
					output = processCollect((Object[]) item.getInput());
					break;
				case EXECUTE:
					output = processExecute((MonitorContext) item.getInput());
					break;
				case TEST:
					output = processTest((TestParams) item.getInput());
					break;
				default:
					throw new IllegalArgumentException(item.getType().toString());
				}
				item.setOutput(output);
			} catch (RuntimeException e) {
				item.setError(e);
			} catch (Throwable e) {
				item.setError(ErrorUtil.createRuntimeException("工作任务执行失败", e));
			} finally {
				finish(item);
			}
		}

		private MonitorResult processTest(TestParams input) {
			return processExecute(new ProbeSideMonitorContext(input, taskService));
		}

		private MonitorResult processExecute(MonitorContext input) {
			Monitor monitor = typeService.checkMonitor(input.getTask().getTypeId());
			return MonitorProcessor.execute(input.getTask(), monitor, input);
		}

		private Serializable processCollect(Object[] input) {
			Monitor monitor = typeService.checkMonitor((String) input[1]);
			CollectParams params = (CollectParams) input[0];
			CollectContext collectContext = new CollectContext(params);
			if (input.length == 3) {
				CollectResult collectResult = (CollectResult) input[2];
				collectContext.setResult(collectResult);
			}
			return monitor.collect(collectContext);
		}

		private void finish(WorkItem item) {
			if (item.getEndTime() == 0) {
				item.stop();
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("工作任务结束执行[%s 执行耗时：%s]", item, Unit.ms.formatPrefer(item.getTime())));
				}
			}

			synchronized (item) {
				item.notify();
			}
		}

		public void abort(WorkItem item, Thread thread) {
			finish(item);
		}

		@Override
		public void destroy() {

		}
	}

	public long getMonitorExecuteCount() {
		return executeCount;
	}

	public CollectResult getCollectResult(String taskId) {
		CollectResult collectResult = collectResultMap.get(taskId);
		return collectResult;
	}

	public CollectTaskSign commitWork(WorkItem work) {
		Object[] object = (Object[]) work.getInput();
		CollectResult result = (CollectResult) object[2];
		CollectParams params = (CollectParams) object[0];
		String nodeId = params.getNode().getId();
		String collectTaskId = getCollectTaskId();
		CollectTaskSign sign = new CollectTaskSign(collectTaskId, nodeId);
		collectResultMap.put(collectTaskId, result);
		workItemMap.put(collectTaskId, work);
		workPipeline.add(work);
		return sign;
	}

	public void cancelCollect(String taskId) {
		WorkItem workItem = workItemMap.get(taskId);
		workPipeline.remove(workItem);
	}

	private String getCollectTaskId() {
		int i = 1;
		i++;
		if (i == Integer.MAX_VALUE - 1)
			i = 1;
		return Integer.toString(i);
	}

	public Object executeWork(WorkItem work) {
		synchronized (work) {
			workPipeline.add(work);
			try {
				work.wait(work.getTimeout());
			} catch (InterruptedException e) {
				throw ErrorUtil.createRuntimeException("执行过程被中断", e);
			}
			if (!work.isFinish())
				throw new RuntimeException("任务执行超时");
			else if (work.getError() != null)
				throw work.getError();
			else
				return work.getOutput();
		}
	}
}
