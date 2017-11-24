package com.broada.carrier.monitor.server.impl.pmdb.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

import com.broada.carrier.monitor.server.impl.pmdb.PMDBFacade;
import com.broada.cmdb.api.client.EventListener;
import com.broada.cmdb.api.data.Instance;
import com.broada.cmdb.api.event.Event;
import com.broada.cmdb.api.event.InstanceChangedEvent;
import com.broada.cmdb.api.event.ObjectChangedType;
import com.broada.cmdb.api.event.RealInstanceChangedEvent;
import com.broada.common.util.cache.CacheConfig;
import com.broada.common.util.cache.CacheManager;
import com.broada.common.util.cache.SimpleCache;
import com.broada.common.util.cache.SimpleKey;

/**
 * 配置项缓存，用于保存一些对象上次的结果
 * @author Jiangjw
 */
public class CICache {
	private SimpleCache cacheByLocalKey;
	private Map<String, String> timeoutCIIds = new ConcurrentHashMap<String, String>();

	public CICache(PMDBFacade pmdbFacade) {		
		CIChangedListener listener = new CIChangedListener();
		pmdbFacade.addCmdbListener(InstanceChangedEvent.class, listener);
		pmdbFacade.addCmdbListener(RealInstanceChangedEvent.class, listener);
	}

	/**
	 * 根据本地获取一个配置项缓存
	 * @param remoteClassCode
	 * @param localKey
	 * @return
	 */
	public CIEntry get(String remoteClassCode, String localKey) {
		SimpleKey key = new SimpleKey(remoteClassCode, localKey);
		CIEntry ciEntry = (CIEntry) getCacheByLocalKey().get(key);
		if (ciEntry != null) {
			boolean timeout = timeoutCIIds.remove(ciEntry.getRemoteKey()) != null;
			if (timeout) {
				getCacheByLocalKey().remove(key);
				return null;
			}
		}
		return ciEntry;
	}	
	
	/**
	 * 添加一个配置项缓存
	 * @param remoteClassCode
	 * @param localKey
	 * @param remoteKey
	 * @param instance
	 */
	public void add(String remoteClassCode, String localKey, String remoteKey, Instance instance) {
		SimpleKey key = new SimpleKey(remoteClassCode, localKey);
		getCacheByLocalKey().getEhcache().put(new Element(key,
				new CIEntry(remoteClassCode, localKey, remoteKey, instance)));		
	}
	
	private SimpleCache getCacheByLocalKey() {
		if (cacheByLocalKey == null) {
			synchronized (this) {
				if (cacheByLocalKey == null) {
					cacheByLocalKey = CacheManager.getDefault().getCache(CacheConfig.createTimeoutCache("pmdb.cache.ci", 1000, false, 600, 600, new CacheEntryFactory() {
						@Override
						public Object createEntry(Object key) throws Exception {
							return null;
						}
					}));
				}
			}
		}
		return cacheByLocalKey;
	}

	private class CIChangedListener implements EventListener {
		@Override
		public void handle(Event event) {
			ObjectChangedType type = ObjectChangedType.CREATED;
			String ciId = null;			
			String user = null;
			if (event instanceof InstanceChangedEvent) {				
				type = ((InstanceChangedEvent) event).getChangedType();
				ciId = ((InstanceChangedEvent) event).getInstanceId();
				user = ((InstanceChangedEvent) event).getChangeUserId();
			} else if (event instanceof RealInstanceChangedEvent) {				
				type = ((RealInstanceChangedEvent) event).getChangedType();
				ciId = ((RealInstanceChangedEvent) event).getInstanceId();
				user = ((RealInstanceChangedEvent) event).getChangeUserId();
			}

			if (ciId != null && type != ObjectChangedType.CREATED) {
				// 如果是更新，并且没有用户的话，说明是monitor本身的更新操作
				if (type == ObjectChangedType.UPDATED && (user == null || user.equals("null"))) 
					return;
				timeoutCIIds.put(ciId, ciId);
			}
		}
	}
}
