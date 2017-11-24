package com.broada.carrier.monitor.common.pool;

import java.util.Date;

import com.broada.component.utils.text.DateUtil;

/**
 * 资源句柄，管理一个具体的资源
 * 
 * @author Jiangjw
 */
public class ResourceHandlerImpl<K, T> implements ResourceHandler<K, T> {
	private ResourceManager<K, T> manager;
	private K key;
	private T resource;
	private long createTime;
	private Thread borrowThread;
	private StackTraceElement[] borrowStack;

	public ResourceHandlerImpl(ResourceManager<K, T> manager, K key, T resource, Thread borrowThread,
			StackTraceElement[] borrowStack) {
		this.manager = manager;
		this.key = key;
		this.resource = resource;
		this.createTime = System.currentTimeMillis();
		this.borrowThread = borrowThread;
		this.borrowStack = borrowStack;
	}

	/**
	 * 获取使用者线程
	 * @return
	 */
	public Thread getBorrowThread() {
		return borrowThread;
	}

	/**
	 * 获取使用者线程堆栈
	 * @return
	 */
	public StackTraceElement[] getBorrowStack() {
		return borrowStack;
	}

	/**
	 * 获取资源建立时间
	 * 
	 * @return
	 */
	public long getCreateTime() {
		return createTime;
	}

	@Override
	public T getResource() {
		return resource;
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public void returnResource() {
		if (resource != null) {
			try {
				manager.returnResource(this);
			} finally {
				resource = null;
			}
		}
	}

	@Override
	public void destroyResource() {
		if (resource != null) {
			try {
				manager.destroyResource(this);
			} finally {
				resource = null;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("%s[key: %s createTime: %s thread: %d %s]", getClass().getSimpleName(), key,
				DateUtil.format(new Date(createTime), DateUtil.PATTERN_YYYYMMDD_HHMMSS), 
				borrowThread.getId(), borrowThread.getName()));
		if (borrowStack != null) {			
			sb.append("\n调用堆栈：");
			for (StackTraceElement s : borrowStack) 
				sb.append("\n\t    at " + s);	
		}
		
		return sb.toString();
	}	
}
