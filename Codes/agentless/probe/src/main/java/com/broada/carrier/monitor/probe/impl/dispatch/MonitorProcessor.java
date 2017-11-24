package com.broada.carrier.monitor.probe.impl.dispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.api.service.ProbeMethodService;
import com.broada.carrier.monitor.probe.api.service.ProbeNodeService;
import com.broada.carrier.monitor.probe.api.service.ProbeResourceService;
import com.broada.carrier.monitor.probe.impl.ProbeStateMonitor;
import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.entity.ProbeSideMonitorContext;
import com.broada.carrier.monitor.probe.impl.logic.ProbeTaskServiceEx;
import com.broada.carrier.monitor.probe.impl.logic.ProbeTypeServiceImpl;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.carrier.monitor.spi.error.MonitorException;
import com.broada.component.utils.error.ErrorUtil;

public class MonitorProcessor {
	private static final Logger logger = LoggerFactory.getLogger(MonitorProcessor.class);
	@Autowired
	private ProbeTaskServiceEx taskService;
	@Autowired
	private ProbeTypeServiceImpl typeService;
	@Autowired
	private ProbeNodeService nodeService;
	@Autowired
	private ProbeResourceService resourceService;
	@Autowired
	private ProbeMethodService methodService;
	
	private static ExecutorService executor = Executors.newFixedThreadPool(2);
	
	private static int waitTimeout = Config.getDefault().getProperty("monitor.wait.timeout", 60);
	
	// 用做上一次正确指标的缓存
	private static ConcurrentHashMap<String, MonitorResult>  ret = new ConcurrentHashMap<String, MonitorResult>();
	
	public static MonitorResult execute(MonitorTask task, final Monitor monitor, final MonitorContext context) {
		try {
			MonitorResult result = new MonitorResult(MonitorState.FAILED);
			if(needCache(task)){
				Future<MonitorResult> future = executor.submit(new Callable<MonitorResult>() {
					@Override
					public MonitorResult call() throws Exception {
						return monitor.monitor(context);
					}
				});
				result = future.get(waitTimeout, TimeUnit.SECONDS);
				ret.put(task.getId(), result);
			}else{
				result = monitor.monitor(context);
			}
			result.setTaskId(task.getId());
			filterInstance(context, result);
			return result;
		} catch (Throwable e) {
			MonitorState state = null;
			String msg = "";
			PerfResult[] perfs = new PerfResult[]{};
			if(e instanceof TimeoutException) {
				if(task.getTypeId().contains("VMWARE") || task.getTypeId().contains("HYPERVISOR")){
					if(task.getId() != null)
						return ret.get(task.getId());
				}
			} else {
				msg = ErrorUtil.createMessage("监测任务执行失败", e);
				logger.warn(ErrorUtil.createMessage("监测任务执行失败：" + task, e), e);
				state = MonitorState.FAILED;
			}
			return new MonitorResult(state, msg, perfs);
		}
	}

	public void process(MonitorTaskItem item) {
		MonitorTask task;
		MonitorContext context;
		Monitor monitor;
		try {
			if (!ProbeStateMonitor.isAvailable())
				throw new IllegalArgumentException("监测任务放弃，当前探针无法连接网络");
		} catch (IllegalArgumentException e) {
			ErrorUtil.warn(logger, "监测任务执行失败：" + item.getTask(), e);
			return;
		}

		MonitorResult result = new MonitorResult();
		try {
			task = taskService.getTask(item.getTask().getId());
			if (task == null)
				throw new IllegalArgumentException("监测任务放弃，已经不存在：" + item.getTask().getId());
			result.setTaskId(task.getId());   // 监测结果里添加任务标识，方便排查
			MonitorNode node = nodeService.getNode(task.getNodeId());
			item.setNode(node);
			if (node == null)
				throw new IllegalArgumentException("监测任务放弃，节点不存在：" + task.getNodeId());

			MonitorResource resource = null;
			if (task.getResourceId() != null) {
				resource = resourceService.getResource(task.getResourceId());
				if (resource == null)
					throw new IllegalArgumentException("监测任务放弃，资源不存在：" + task.getResourceId());
			}

			MonitorMethod method = null;
			if (task.getMethodCode() != null) {
				method = methodService.getMethod(task.getMethodCode());
				if (method == null) {
					logger.info("监测方法为null, methodService class:{}, methodCode:{}", methodService.getClass(),
							task.getMethodCode());
					throw new IllegalArgumentException("监测任务放弃，方法不存在：" + task.getMethodCode());
				}
			}

			context = new ProbeSideMonitorContext(node, resource, method, task, taskService.getInstancesByTaskId(task
					.getId()), taskService, item.getPolicy(), item.getRecord());
			monitor = typeService.checkMonitor(task.getTypeId());
			if (!task.isEnabled())
				return;
			result = execute(task, monitor, context);
			logger.info("监测结果信息: {}", result);
		} catch (Throwable e) {
			String msg;
			if (e instanceof MonitorException) {
				msg = e.getMessage();
				if (logger.isDebugEnabled())
					logger.debug("监测任务执行失败：" + item.getTask(), e);
			} else {
				msg = ErrorUtil.createMessage("监测任务执行失败", e);
				logger.warn(ErrorUtil.createMessage("监测任务执行失败：" + item.getTask(), e), e);
			}
			result = new MonitorResult(MonitorState.FAILED, msg, null);
		}

		try {
			result.setTaskId(item.getTask().getId());
			result.setTime(MonitorResultUploader.getDefault().getServerTime());
			item.getRecord().set(result, null);
			taskService.saveRecord(item.getRecord());
			MonitorResultUploader.getDefault().upload(result);
		} catch (Throwable e) {
			ErrorUtil.warn(logger, "监测任务执行失败，监测框架错误", e);
		}
	}

	private static void filterInstance(MonitorContext context, MonitorResult result) {
		if (context.getInstances() == null || context.getInstances().length == 0 || result.getRows() == null
				|| result.getRows().isEmpty())
			return;

		List<MonitorResultRow> needDeletes = new ArrayList<MonitorResultRow>();
		for (MonitorResultRow row : result.getRows()) {
			MonitorInstance inst = context.getInstanceByCode(row.getInstCode());
			if (inst == null)
				needDeletes.add(row);
			else
				row.setInstName(inst.getName());
		}
		result.getRows().removeAll(needDeletes);
	}
	
	/**
	 * 是否需要缓存结果
	 * @param task
	 * @return
	 */
	private static boolean needCache(MonitorTask task){
		if(task.getTypeId().contains("VMWARE") || task.getTypeId().contains("HYPERVISOR"))
			return true;
		else
			return false;
	}
	
}
