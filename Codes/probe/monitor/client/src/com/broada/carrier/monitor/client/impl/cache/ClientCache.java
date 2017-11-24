package com.broada.carrier.monitor.client.impl.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.client.impl.task.TaskTableRow;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;

public class ClientCache {
	/**
	 * key为task id
	 */
	private static Map<String, TaskTableRow> cacheRows = new ConcurrentHashMap<String, TaskTableRow>();
	/**
	 * key为node id
	 */
	private static Map<String, MonitorNode> nodeMap = new ConcurrentHashMap<String, MonitorNode>();
	/**
	 * key为resource id
	 */
	private static Map<String, MonitorResource> resourceMap = new ConcurrentHashMap<String, MonitorResource>();
	private static final Logger logger = LoggerFactory.getLogger(ClientCache.class);
	private static List<CacheReloadListener> lisList = new ArrayList<CacheReloadListener>();
	
	private ClientCache(){
	}
	
	/**
	 * 注册缓存reload监听，在缓存reload之后将调用refresh方法
	 * @param lis
	 */
	public static synchronized void registerCacheReload(CacheReloadListener lis){
		lisList.add(lis);
	}
	
	public static Map<String, TaskTableRow> getCacheRows() {
		return cacheRows;
	}
	
	public static Map<String, MonitorNode> getNodeMap() {
		return nodeMap;
	}

	public static Map<String, MonitorResource> getResourceMap() {
		return resourceMap;
	}
	
	/**
	 * 刷新client缓存
	 */
	public static void reloadCache(){
		resourceMap.clear();
		nodeMap.clear();
		cacheRows.clear();
		try {
			loadCache();
			for(CacheReloadListener lis : lisList)
				lis.refresh();
		} catch (Exception e) {
			logger.error("client缓存未能建立", e);
		}
	}

	private static void loadCache() throws Exception{
		//取得所有的task信息
		MonitorTask[] tasks = ServerContext.getTaskService().getTasks(PageNo.ALL, true).getRows();
		if(tasks == null)
			tasks = new MonitorTask[0];
		
		//取得所有的node信息
		MonitorNode[] nodes = ServerContext.getNodeService().getNodes(true).getRows();
		if(nodes != null)
			for(MonitorNode node : nodes){
				nodeMap.put(node.getId(), node);
			}
		
		//取得所有的resource信息
		String [] idArray = nodeMap.keySet().toArray(new String[0]);
		String ids = StringUtils.join(idArray, ",");
		MonitorResource[] resources = ServerContext.getResourceService().getResourcesByNodeIds(ids).getRows();
		if(resources != null)
			for(MonitorResource resource : resources){
				resourceMap.put(resource.getId(), resource);
			}
		
		//取得所有的record信息
		List<String> taskIds = new ArrayList<String>(); 
		for(MonitorTask task : tasks){
			taskIds.add(String.valueOf(task.getId()));
		}
		Map<String, MonitorRecord> mapRecords = new HashMap<String, MonitorRecord>();
		MonitorRecord[] records = ServerContext.getTaskService().getRecords(StringUtils.join(taskIds, ","));
		if(records != null)
			for(MonitorRecord record : records){
				mapRecords.put(record.getTaskId(), record);
			}
		
		for(MonitorTask task : tasks){
			// 去除node id为null
			if(task.getNodeId() == null)
				continue;
			// 去除resource id不为null,但resource已删除
			if(task.getResourceId() != null && resourceMap.get(task.getResourceId()) == null)
				continue;
			TaskTableRow row = new TaskTableRow(task, nodeMap.get(task.getNodeId()), task.getResourceId() == null ? null : resourceMap.get(task.getResourceId()), mapRecords.get(task.getId()));
			cacheRows.put(task.getId(), row);
		}
	}
}
