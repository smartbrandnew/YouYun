package com.broada.carrier.monitor.common.pool;

/**
 * 资源操作句柄
 * @author Jiangjw
 */
public interface ResourceHandler<K, T> {
	/**
	 * 获取资源对象
	 * 
	 * @return
	 */
	T getResource();

	/**
	 * 获取关键参数
	 * 
	 * @return
	 */
	K getKey();	
	
	/**
	 * 归还资源
	 */
	void returnResource();

	/**
	 * 销毁资源
	 */
	void destroyResource();
}
