package com.broada.carrier.monitor.server.api.client;


import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.server.api.error.TargetNotExistsException;
import com.broada.carrier.monitor.server.api.service.BaseMethodService;
import com.broada.carrier.monitor.server.api.service.BaseNodeService;
import com.broada.carrier.monitor.server.api.service.BasePolicyService;
import com.broada.carrier.monitor.server.api.service.BaseResourceService;
import com.broada.carrier.monitor.server.api.service.BaseTaskService;
import com.broada.carrier.monitor.server.api.service.BaseTypeService;

public class BaseServiceUtil {
	public static MonitorNode checkNode(BaseNodeService nodeService, String nodeId) {
		MonitorNode node = nodeService.getNode(nodeId);
		if (node == null)
			throw new TargetNotExistsException("监测节点", nodeId);
		return node;
	}

	public static MonitorResource checkResource(BaseResourceService resourceService, String resourceId) {
		MonitorResource resource = resourceService.getResource(resourceId);
		if (resource == null)
			throw new TargetNotExistsException("监测资源", resourceId);
		return resource;
	}

	public static MonitorTask checkTask(BaseTaskService taskService, String taskId) {
		MonitorTask task = taskService.getTask(taskId);
		if (task == null)
			throw new IllegalArgumentException("监测任务不存在：" + taskId);
		return task;
	}

	public static MonitorPolicy checkPolicy(BasePolicyService policyService, String policyCode) {
		MonitorPolicy policy = policyService.getPolicy(policyCode);
		if (policy == null)
			throw new IllegalArgumentException("监测策略不存在：" + policy);
		return policy;
	}

	public static MonitorMethod checkMethod(BaseMethodService methodService, String methodCode) {
		MonitorMethod method = methodService.getMethod(methodCode);
		if (method == null)
			throw new IllegalArgumentException("监测方法不存在：" + methodCode);
		return method;
	}

	public static MonitorType checkType(BaseTypeService typeService, String typeId) {
		MonitorType type = typeService.getType(typeId);
		if (type == null)
			throw new IllegalArgumentException("监测类型不存在：" + typeId);
		return type;
	}

	public static MonitorItem checkItem(BaseTypeService typeService, String itemCode) {
		MonitorItem item = typeService.getItem(itemCode);
		if (item == null)
			throw new IllegalArgumentException("监测指标不存在：" + itemCode);
		return item;
	}

	public static MonitorMethodType checkMethodType(BaseTypeService typeService, String typeId) {
		MonitorMethodType type = typeService.getMethodType(typeId);
		if (type == null)
			throw new IllegalArgumentException("监测方法类型不存在：" + typeId);
		return type;
	}
}
