package com.broada.carrier.monitor.server.impl.pmdb.map;

import org.hibernate.cache.ehcache.management.impl.BeanUtils;

import com.broada.carrier.monitor.common.util.BeanUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.service.BaseMethodService;
import com.broada.carrier.monitor.server.api.service.ServerResourceService;

public class MapInput {
	private ServerResourceService resourceService;
	private BaseMethodService methodService;
	private MonitorNode node;
	private MonitorTask task;
	private MonitorResult result;
	private MonitorResultRow instance;
	private MonitorResource resource;

	public MapInput(ServerResourceService resourceService, BaseMethodService methodService, MonitorNode node, MonitorTask task, MonitorResult result) {
		this.resourceService = resourceService;
		this.methodService = methodService;
		this.node = node;
		this.task = task;
		this.result = result;
	}

	public MonitorNode getNode() {
		return node;
	}

	public MonitorTask getTask() {
		return task;
	}

	public MonitorResult getResult() {
		return result;
	}

	public MonitorResultRow getInstance() {
		return instance;
	}

	public void setInstance(MonitorResultRow instance) {
		this.instance = instance;
	}

	public MonitorResource getResource() {
		if (resource == null) {
			if (task.getResourceId() != null)
				resource = resourceService.getResource(task.getResourceId());
		}
		return resource;
	}

	@Override
	public String toString() {
		return String.format("%s[task: %s result: %s]", getClass().getSimpleName(), getTask(), getResult());
	}	
	
	public Object getValue(String local) {
		MapItemLocal item = new MapItemLocal(local);
		return getValue(item.getType(), item.getCode());
	}
	
	public Object getValue(MapObjectType localType, String localCode) {
		switch (localType) {
		case NODE:
			return getNodeValue(localCode);
		case RESOURCE:
			return getResourceValue(localCode);
		case MONITOR:
			return getMonitorValue(localCode);			
		case METHOD:
			return getMethodValue(localCode);
		case INSTANCE:
			return getInstanceValue(localCode);
		case PERF:
			return getPerfValue(localCode);
		case RESULT:
			return getResultValue(localCode);
		default:
			throw new IllegalArgumentException(localType.toString());
		}
	}
	
	private Object getMethodValue(String localCode) {
		if (task.getMethodCode() == null)
			return null;
		
		MonitorMethod method = methodService.getMethod(task.getMethodCode());
		if (method == null)
			return null;
		
		return method.getProperties().get(localCode);
	}

	private Object getPerfValue(String localCode) {
		if (getInstance() == null)
			return null;
		return getInstance().getIndicator(localCode);
	}
	
	private Object getMonitorValue(String localCode) {
		if (localCode.equalsIgnoreCase("state"))
			return getResult().getState();
		return BeanUtils.getBeanProperty(getTask(), localCode);
	}
	
	private Object getResultValue(String localCode) {
		return BeanUtil.getPropertyValue(getResult(), localCode);
	}

	private Object getInstanceValue(String localCode) {
		return BeanUtil.getPropertyValue(getInstance(), localCode);
	}

	private Object getResourceValue(String localCode) {
		return BeanUtil.getPropertyValue(getResource(), localCode);
	}

	private Object getNodeValue(String localCode) {
		return BeanUtil.getPropertyValue(getNode(), localCode);
	}
	
	private static String getInstanceLocalKey(String taskId, String instCode) {
		return "task." + taskId + ".inst." + instCode;
	}

	public String getLocalKey(MapObjectType localType) {
		switch (localType) {
		case NODE:
			return "node." + getNode().getId();
		case RESOURCE:
			return "res." + getResource().getId();
		case INSTANCE:
			return getInstanceLocalKey(getResult().getTaskId(), getInstance().getInstCode());
		default:
			throw new IllegalArgumentException(localType.toString());
		}
	}
}
