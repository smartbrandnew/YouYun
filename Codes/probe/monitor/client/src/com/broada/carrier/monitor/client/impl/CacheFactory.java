package com.broada.carrier.monitor.client.impl;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

import com.broada.common.util.cache.CacheConfig;
import com.broada.common.util.cache.CacheManager;
import com.broada.common.util.cache.SimpleCache;

public class CacheFactory {
	public static final int TEMP_CACHE_MAX_ELEMENTS_ = 500;
	public static final int TEMP_CACHE_TIMEOUT_SECONDS = 10 * 60;
	
	/**
	 * 建立一个临时缓存，元素默认超时为10分钟
	 * @param name
	 * @param cacheEntryFactory
	 * @return
	 */
	public static SimpleCache createTempCache(String name, CacheEntryFactory cacheEntryFactory) {
		return createTempCache(name, cacheEntryFactory, TEMP_CACHE_TIMEOUT_SECONDS);
	}	
	
	/**
	 * 建立一个临时缓存
	 * @param name
	 * @param cacheEntryFactory
	 * @param timeout 超时时间
	 * @return
	 */
	public static SimpleCache createTempCache(String name, CacheEntryFactory cacheEntryFactory, int timeout) {
		return CacheManager.getDefault().getCache(
				CacheConfig.createTimeoutCache(name, TEMP_CACHE_MAX_ELEMENTS_, false, timeout, 0, cacheEntryFactory));
	}	
}
