package com.broada.carrier.monitor.client.impl.common;

import java.util.ArrayList;
import java.util.List;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.client.impl.cache.ClientCache;
import com.broada.carrier.monitor.client.impl.node.NodeTreeNode;
import com.broada.carrier.monitor.client.impl.resource.ResourceTreeNode;
import com.broada.carrier.monitor.client.impl.target.TargetTableRow;
import com.broada.carrier.monitor.client.impl.task.TaskTableRow;
import com.broada.carrier.monitor.common.swing.action.Action;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;

public abstract class ActionTasks extends Action {
	private MonitorTask[] getTasks(Object obj) {
		MonitorTask[] tasks = null;
		if (obj instanceof MonitorNode)
			tasks = ServerContext.getTaskService().getTasksByNodeId(((MonitorNode) obj).getId());
		else if (obj instanceof MonitorResource)
			tasks = ServerContext.getTaskService().getTasksByResourceId(((MonitorResource) obj).getId());
		else if (obj instanceof MonitorTask)
			tasks = new MonitorTask[] { (MonitorTask) obj };
		else if (obj instanceof TaskTableRow)
			tasks = new MonitorTask[] { ((TaskTableRow) obj).getTask() };
		else if (obj instanceof List<?>) {			
			List<?> list = (List<?>) obj;
			if (list.size() > 0) {
				obj = list.get(0);
				if (obj instanceof TaskTableRow) {
					tasks = new MonitorTask[list.size()];
					for (int i = 0; i < tasks.length; i++)
						tasks[i] = ((TaskTableRow)list.get(i)).getTask();					
				} else if (obj instanceof TargetTableRow) {
					List<MonitorTask> tasksList = new ArrayList<MonitorTask>();
					for (Object item : list) {
						MonitorTask[] array = getTasks(item);
						if (array != null) {
							for (MonitorTask task : array)
								tasksList.add(task);
						}
					}
					tasks = tasksList.toArray(new MonitorTask[tasksList.size()]);
				} else 
					throw new IllegalArgumentException("未知的操作对象类型：" + obj.getClass().getName());
			}
		}	else if (obj instanceof NodeTreeNode)
			return getTasks(((NodeTreeNode) obj).getNode());
		else if (obj instanceof ResourceTreeNode)
			return getTasks(((ResourceTreeNode) obj).getResource());
		else if (obj instanceof TargetTableRow)
			return getTasks(((TargetTableRow) obj).getTarget());
		return tasks;
	}

	@Override
	public void execute(Object obj) {
		MonitorTask[] tasks = getTasks(obj);
		if (tasks == null)
			return;
		for (MonitorTask task : tasks)
			execute(task);
		if(ifReloadCacheAfterExecute())
			ClientCache.reloadCache();
	}

	protected abstract void execute(MonitorTask task);
	
	/**
	 * execute之后是否刷新cache
	 * @return
	 */
	protected abstract boolean ifReloadCacheAfterExecute();

	@Override
	public boolean isVisible(Object obj) {
		if (obj instanceof MonitorNode
				|| obj instanceof MonitorResource
				|| obj instanceof MonitorTask
				|| obj instanceof TaskTableRow
				|| obj instanceof List<?>
				|| obj instanceof NodeTreeNode
				|| obj instanceof ResourceTreeNode
				|| obj instanceof TargetTableRow)
			return true;
		return false;
	}
}
