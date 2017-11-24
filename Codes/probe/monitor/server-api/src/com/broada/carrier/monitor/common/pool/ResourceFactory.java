package com.broada.carrier.monitor.common.pool;

/**
 * 资源工厂接口
 * @author Jiangjw
 */
public interface ResourceFactory<K, T> {
	/**
	 * 获取资源
	 * @param key
	 * @return
	 */
	T borrowResource(K key);
	
	/**
	 * 归并资源
	 * @param key
	 * @param resource
	 */
	void returnResource(K key, T resource);
	
	/**
	 * 销毁资源。
	 * 用于发现资源已经不可用时的强制销毁手段。
	 * 一般在使用资源池时，会存在资源归还时是否直接销毁的需求
	 * @param key
	 * @param resource
	 */
	void destroyResource(K key, T resource);
}
