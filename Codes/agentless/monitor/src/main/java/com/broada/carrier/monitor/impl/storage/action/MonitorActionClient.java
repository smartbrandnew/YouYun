package com.broada.carrier.monitor.impl.storage.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.broada.cid.action.api.entity.ActionMetadata;
import com.broada.cid.action.api.entity.ActionRequest;
import com.broada.cid.action.api.entity.ActionResponse;
import com.broada.cid.action.api.entity.ProtocolMetadata;
import com.broada.cid.action.api.service.ActionServiceFactory;

/**
 * 对Action提供的服务进行基本的包装以更适用于Monitor监测
 */
public class MonitorActionClient {
	private static MonitorActionMetadata[] actions;
	
	/**
	 * 获取所有Action定义
	 * @return
	 */
	public static MonitorActionMetadata[] getActions(String outputCIType) {
		List<MonitorActionMetadata> result = new ArrayList<MonitorActionMetadata>();
		MonitorActionMetadata[] actions = getActions();		
		for (MonitorActionMetadata action : actions) {
			if (outputCIType.equalsIgnoreCase(action.getOutput()))
				result.add(action);
		}
		return result.toArray(new MonitorActionMetadata[0]);
	}	

	public static MonitorActionMetadata[] getActions() {
		if (actions == null) {
			synchronized (MonitorActionClient.class) {
				if (actions == null) {
					ActionMetadata[] orgActions = ActionServiceFactory.getActionService().getActionMetadatas();
					List<MonitorActionMetadata> result = new ArrayList<MonitorActionMetadata>(orgActions.length);
					for (ActionMetadata action : orgActions) {
						result.add(new MonitorActionMetadata(action));
					}
					Collections.sort(result, new MonitorActionMetadata.PriorityComparator());
					actions = result.toArray(new MonitorActionMetadata[result.size()]);				
				}
			}
		}
		return actions;
	}

	/**
	 * 获取所有Protocol定义
	 * @return
	 */
	public static ProtocolMetadata[] getProtocolMetadatas() {
		return ActionServiceFactory.getActionService().getProtocolMetadatas();
	}

	/**
	 * 执行Action
	 * @param param
	 * @return
	 */
	public static ActionResponse execute(ActionRequest request) {
		return ActionServiceFactory.getActionService().execute(request);
	}
	
	/**
	 * 结束脚本执行
	 */
	public static void shutdown() {
		ActionServiceFactory.getActionService().shutdown();
	}
}
