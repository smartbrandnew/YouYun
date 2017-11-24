package com.broada.carrier.monitor.server.impl.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.util.TextUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.service.ServerNodeService;
import com.broada.carrier.monitor.server.api.service.ServerResourceService;
import com.broada.carrier.monitor.server.impl.config.Config;
import com.broada.common.util.Unit;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.lang.ThreadUtil;

public class ServerTaskCleaner implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(ServerTaskCleaner.class);
	@Autowired
	private ServerTaskServiceEx taskService;
	@Autowired
	private ServerNodeService nodeService;
	@Autowired
	private ServerResourceService resourceService;
	
	public ServerTaskCleaner() {
		ThreadUtil.createThread(this).start();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(getDelay());
			while (true) {
				clean();
				logger.debug("任务清理线程睡眠：{}ms", getInterval());
				Thread.sleep(getInterval());
			}
		} catch (InterruptedException e) {
			ErrorUtil.warn(logger, "无用任务清理线程关闭", e);
		}
	}

	private long getDelay() {
		return Config.getDefault().getProps().get("monitor.task.clean.delay", 60l) * 1000;
	}

	private long getInterval() {
		return Config.getDefault().getProps().get("monitor.task.clean.interval", 24 * 60 * 60l) * 1000;
	}

	private void clean() {
		logger.debug("任务清理开始");
		
		long start = System.currentTimeMillis();		
		int nodeDeletedCount = 0;
		String[] nodeIds = taskService.getTaskNodeIds();
		for (String nodeId : nodeIds) {
			try {
				MonitorNode node = nodeService.getNode(nodeId);
				if (node == null) {
					taskService.deleteTaskByNodeId(nodeId);
					nodeDeletedCount++;
				}
			} catch (Throwable e) {
				ErrorUtil.warn(logger, "任务清理失败，节点：" + nodeId, e);
			}
		}
		
		int resourceDeletedCount = 0;
		String[] resourceIds = taskService.getTaskResourceIds();
		for (String resourceId : resourceIds) {
			if (TextUtil.isEmpty(resourceId))
				continue;
			try {
				MonitorResource resource = resourceService.getResource(resourceId);
				if (resource == null) {
					taskService.deleteTaskByResourceId(resourceId);
					resourceDeletedCount++;
				}
			} catch (Throwable e) {
				ErrorUtil.warn(logger, "任务清理失败，资源：" + resourceId, e);
			}
		}
		
		logger.info(String.format("任务清理完成，共检查节点%s个、资源%s个，删除节点%s个、资源%s个，耗时%s", 
				nodeIds.length, resourceIds.length, nodeDeletedCount, resourceDeletedCount, 
				Unit.ms.formatPrefer(System.currentTimeMillis() - start)));
	}
}
