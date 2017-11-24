package com.broada.carrier.monitor.client.impl;

import javax.swing.JOptionPane;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

import com.broada.carrier.monitor.client.impl.config.Config;
import com.broada.carrier.monitor.server.api.client.EventClient;
import com.broada.carrier.monitor.server.api.client.EventListener;
import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
import com.broada.carrier.monitor.server.api.client.restful.RestfulServerServiceFactory;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.error.TargetNotExistsException;
import com.broada.carrier.monitor.server.api.event.NodeChangedEvent;
import com.broada.carrier.monitor.server.api.event.ObjectChangedEvent;
import com.broada.carrier.monitor.server.api.event.ResourceChangedEvent;
import com.broada.carrier.monitor.server.api.service.ServerMethodService;
import com.broada.carrier.monitor.server.api.service.ServerNodeService;
import com.broada.carrier.monitor.server.api.service.ServerPolicyService;
import com.broada.carrier.monitor.server.api.service.ServerProbeService;
import com.broada.carrier.monitor.server.api.service.ServerResourceService;
import com.broada.carrier.monitor.server.api.service.ServerSystemService;
import com.broada.carrier.monitor.server.api.service.ServerTargetGroupService;
import com.broada.carrier.monitor.server.api.service.ServerTargetTypeService;
import com.broada.carrier.monitor.server.api.service.ServerTaskService;
import com.broada.carrier.monitor.server.api.service.ServerTypeService;
import com.broada.common.util.cache.SimpleCache;

public class ServerContext {
	private static RestfulServerServiceFactory serverFactory;
	private static String serverIp;
	private static EventClient eventClient;
	private static SimpleCache cacheTargetType;
	private static SimpleCache cacheMethodType;
	private static SimpleCache cacheNode;
	private static SimpleCache cacheResource;	

	/**
	 * 获取默认实例
	 * @return
	 */
	public static ServerServiceFactory getServerFactory() {
		if (serverFactory == null) 
			throw new IllegalStateException("系统还未建立连接");
		return serverFactory;
	}
	
	public static EventClient getEventClient() {
		if (eventClient == null) 
			throw new IllegalStateException("系统还未建立连接");
		return eventClient;
	}
	
	public static boolean isConnected() {
		return eventClient != null;
	}

	public static void connect(String ip) {
		int port = 8890;
		int portPos = ip.indexOf(":");
		if (portPos > 0) {
			serverIp = ip.substring(0, portPos);
			String text = ip.substring(portPos + 1);
			if (!text.isEmpty())
				port = Integer.parseInt(text);			
		} else
			serverIp = ip;
	
		RestfulServerServiceFactory temp = new RestfulServerServiceFactory(getIp(), port);
		temp.getSystemService().getTime();		
		serverFactory = temp;
		
		eventClient = new EventClient(Config.getDefault().getMQUrl(), Config.getDefault().getMQUser(), Config.getDefault().getMQPassword(), "carrier.monitor.client" + System.currentTimeMillis());		
		
		Config.getDefault().initServerConfig(getSystemService());
		
		registerObjectChangedListener(new CacheCleanListener());
	}
	
	private static class CacheCleanListener implements EventListener {
		@Override
		public void receive(Object event) {
			if (event instanceof NodeChangedEvent)
				getCacheNode().remove(((NodeChangedEvent) event).getObject().getId());
			else if (event instanceof ResourceChangedEvent)
				getCacheResource().remove(((ResourceChangedEvent) event).getObject().getId());
		}
	}
	
	public static String getIp() {
		return serverIp;
	}
	
	public static ServerSystemService getSystemService() {
		return getServerFactory().getSystemService();
	}
	
	public static ServerTaskService getTaskService() {
		return getServerFactory().getTaskService();
	}
	
	public static ServerNodeService getNodeService() {
		return getServerFactory().getNodeService();
	}
	
	public static ServerResourceService getResourceService() {
		return getServerFactory().getResourceService();
	}

	public static ServerProbeService getProbeService() {
		return getServerFactory().getProbeService();
	}

	public static ServerTargetTypeService getTargetTypeService() {
		return getServerFactory().getTargetTypeService();
	}

	public static ServerTypeService getTypeService() {
		return getServerFactory().getTypeService();
	}

	public static ServerMethodService getMethodService() {
		return getServerFactory().getMethodService();
	}

	public static ServerPolicyService getPolicyService() {
		return getServerFactory().getPolicyService();
	}

	public static void registerObjectChangedListener(EventListener listener) {
		getEventClient().addListener(ObjectChangedEvent.TOPIC, listener.getClass().getName(), listener);
	}

	public static ServerTargetGroupService getTargetGroupService() {
		return getServerFactory().getTargetGroupService();
	}

	public static void login(String username, String passwd) {
		getServerFactory().login(username, passwd);
	}
	
	public static void logout() {
		getServerFactory().logout();
	}

	public static OperatorResult deleteResource(String id) {
		if (JOptionPane.showConfirmDialog(MainWindow.getDefault(), "删除资源将删除导致其下所有资源与监测任务，请问是否确定？", "操作确认",
				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return OperatorResult.NONE;
		return getResourceService().deleteResource(id);
	}

	public static OperatorResult deleteNode(String id) {
		if (JOptionPane.showConfirmDialog(MainWindow.getDefault(), "删除节点将删除导致其下所有资源与监测任务，请问是否确定？", "操作确认",
				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return OperatorResult.NONE;
		return getNodeService().deleteNode(id);
	}
	
	/**
	 * 从本地缓存中获取targetType
	 * @param id
	 * @return
	 */
	public static MonitorTargetType getTargetType(String id) {
		return (MonitorTargetType) getCacheTargetType().get(id);
	}
	
	/**
	 * 从本地缓存中获取targetType
	 * @param id
	 * @return
	 */
	public static MonitorTargetType checkTargetType(String id) {
		MonitorTargetType targetType = getTargetType(id);
		if (targetType == null)
			throw new TargetNotExistsException("监测项类型", id);
		return targetType;
	}	
	
	/**
	 * 从本地缓存中获取node
	 * @param id
	 * @return
	 */
	public static MonitorNode getNode(String id) {
		return (MonitorNode) getCacheNode().get(id);
	}	
	
	/**
	 * 从本地缓存中获取node
	 * @param id
	 * @return
	 */
	public static MonitorNode checkNode(String id) {
		MonitorNode node = getNode(id);
		if (node == null)
			throw new TargetNotExistsException("监测节点", id);
		return node;
	}
	
	/**
	 * 从本地缓存中获取resource
	 * @param id
	 * @return
	 */
	public static MonitorResource checkResource(String id) {
		MonitorResource resource = getResource(id);
		if (resource == null)
			throw new TargetNotExistsException("监测资源", id);
		return resource;
	}
	
	/**
	 * 从本地缓存中获取resource
	 * @param id
	 * @return
	 */
	public static MonitorResource getResource(String id) {
		return (MonitorResource) getCacheResource().get(id);
	}	
	
	private static SimpleCache getCacheMethodType() {
		if (cacheMethodType == null) {
			cacheMethodType = CacheFactory.createTempCache("cacheMethodType", new CacheEntryFactory() {				
				@Override
				public Object createEntry(Object arg0) throws Exception {
					return getTypeService().getMethodType((String) arg0);	
				}
			}, 60);
		}
		return cacheMethodType;
	}
	
	private static SimpleCache getCacheTargetType() {
		if (cacheTargetType == null) {
			cacheTargetType = CacheFactory.createTempCache("cacheTargetType", new CacheEntryFactory() {				
				@Override
				public Object createEntry(Object arg0) throws Exception {
					return getTargetTypeService().getTargetType((String) arg0);					
				}
			}, 60);
		}
		return cacheTargetType;
	}

	private static SimpleCache getCacheNode() {
		if (cacheNode == null) {
			cacheNode = CacheFactory.createTempCache("cacheNode", new CacheEntryFactory() {				
				@Override
				public Object createEntry(Object arg0) throws Exception {
					return getNodeService().getNode((String) arg0);
				}
			});
		}
		return cacheNode;
	}
	
	private static SimpleCache getCacheResource() {
		if (cacheResource == null) {
			cacheResource = CacheFactory.createTempCache("cacheResource", new CacheEntryFactory() {				
				@Override
				public Object createEntry(Object arg0) throws Exception {
					return getResourceService().getResource((String) arg0);
				}
			});
		}
		return cacheResource;
	}
	
	public static String getServerUrl() {
		return serverFactory.getBaseServiceUrl(); 
	}

	public static MonitorMethodType checkMethodType(String typeId) {
		MonitorMethodType methodType = (MonitorMethodType) getCacheMethodType().get(typeId);
		if (methodType == null)
			throw new TargetNotExistsException("监测协议类型", typeId);
		return methodType;
	}
}
