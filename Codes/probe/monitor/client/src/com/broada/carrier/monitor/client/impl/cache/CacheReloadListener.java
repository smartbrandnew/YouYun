package com.broada.carrier.monitor.client.impl.cache;

public interface CacheReloadListener {
	
	/**
	 * 该方法将在cache reload之后调用
	 */
	void refresh();
}
