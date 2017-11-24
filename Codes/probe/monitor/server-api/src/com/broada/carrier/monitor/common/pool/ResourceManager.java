package com.broada.carrier.monitor.common.pool;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.lang.ThreadUtil;

/**
 * 资源管理器
 * @author Jiangjw
 */
public class ResourceManager<K, T> {
	private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);
	private ResourceFactory<K, T> factory;
	private List<ResourceHandlerImpl<K, T>> handlers = new LinkedList<ResourceHandlerImpl<K, T>>();
	private String name;
	private long timeout;
	private Thread thread;

	public ResourceManager(String name, long timeout, ResourceFactory<K, T> factory) {
		super();
		this.name = name;
		this.timeout = timeout;
		this.factory = factory;
		startup();
	}

	/**
	 * 资源管理器是否正在运行
	 * @return
	 */
	public boolean isRunning() {
		return thread != null;
	}

	/**
	 * 启动垃圾资源回收
	 */
	public void startup() {
		if (isRunning())
			return;

		thread = ThreadUtil.createThread(new ResourceCleaner(), "ResourceManager-" + name);
		thread.start();
	}

	/**
	 * 关闭垃圾资源回收
	 */
	public void shutdown() {
		if (!isRunning())
			return;

		Thread thread = this.thread;
		this.thread = null;
		thread.interrupt();
	}

	/**
	 * 获取资源池名称
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 获取资源超时时间，单位ms
	 * @return
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * 获取资源
	 * @param key
	 * @return
	 */
	public ResourceHandler<K, T> borrowResource(K key) {
		T resource = factory.borrowResource(key);
		if (resource == null)
			return null;
		Thread borrowThread = Thread.currentThread();
		StackTraceElement[] borrowStack = null;
		if (logger.isDebugEnabled())
			borrowStack = borrowThread.getStackTrace();
		ResourceHandlerImpl<K, T> handler = new ResourceHandlerImpl<K, T>(this, key, resource, borrowThread, borrowStack);
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("资源管理器[%s]申请资源%s", name, handler));
		}
		addHandler(handler);
		return handler;
	}

	private synchronized void addHandler(ResourceHandlerImpl<K, T> handler) {
		handlers.add(handler);
	}

	private synchronized boolean removeHandler(ResourceHandlerImpl<K, T> handler) {
		return handlers.remove(handler);
	}

	private synchronized void cleanHandlers() {
		long now = System.currentTimeMillis();
		if (logger.isDebugEnabled())
			logger.debug(String.format("资源管理器[%s]共有资源[%d]", name, handlers.size()));
		for (Iterator<ResourceHandlerImpl<K, T>> iter = handlers.iterator(); iter.hasNext();) {
			ResourceHandlerImpl<K, T> handler = iter.next();
			try {
				if (isTimeout(handler, now)) {
					destroyResourceInner(handler, "资源超时", true);
					iter.remove();
				}
			} catch (Throwable e) {
				ErrorUtil.warn(logger, "释放资源失败：" + handler, e);
			}
		}
	}

	private boolean isTimeout(ResourceHandlerImpl<K, T> handler, long now) {
		long time = now - handler.getCreateTime();
		return time >= timeout;
	}

	/**
	 * 归还资源
	 * @param handler
	 */
	public void returnResource(ResourceHandler<K, T> handler) {
		if (removeHandler((ResourceHandlerImpl<K, T>) handler)) {
			if (logger.isDebugEnabled()) 
				logger.debug(String.format("资源管理器[%s]归还资源%s", name, handler));				
			factory.returnResource(handler.getKey(), handler.getResource());
		}
	}

	/**
	 * 销毁资源
	 * @param handler
	 */
	public void destroyResource(ResourceHandler<K, T> handler) {
		if (removeHandler((ResourceHandlerImpl<K, T>) handler))
			destroyResourceInner(handler, "业务销毁", false);		
	}

	private void destroyResourceInner(ResourceHandler<K, T> handler, String message, boolean warn) {
		if (warn)
			logger.warn(String.format("资源管理器[%s]，由于[%s]，销毁资源%s", name, message, handler));
		else if (logger.isDebugEnabled())
			logger.debug(String.format("资源管理器[%s]，由于[%s]，销毁资源%s", name, message, handler));
		factory.destroyResource(handler.getKey(), handler.getResource());
	}

	private class ResourceCleaner implements Runnable {

		@Override
		public void run() {
			long interval = Math.min(Math.max(timeout / 10, 50), 60 * 1000);
			while (isRunning()) {
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					break;
				}

				cleanHandlers();
			}
		}

	}
}
