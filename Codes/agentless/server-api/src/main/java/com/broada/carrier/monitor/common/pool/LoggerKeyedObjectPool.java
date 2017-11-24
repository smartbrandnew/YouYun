package com.broada.carrier.monitor.common.pool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedPoolableObjectFactory;

/**
 * 日志化的Pool，便于进行资源泄露的调试
 * @author Jiangjw
 */
public class LoggerKeyedObjectPool implements KeyedObjectPool {
	private static final Log logger = LogFactory.getLog(LoggerKeyedObjectPool.class);
	private String id;
	private KeyedObjectPool pool;
	
	public LoggerKeyedObjectPool(String id, KeyedObjectPool pool) {
		super();
		this.id = id;
		this.pool = pool;
	}

	@Override
	public void addObject(Object arg0) throws Exception {
		pool.addObject(arg0);
	}
	
	private String getLoggerMessage(Object key, String action) {
		Thread thread = Thread.currentThread();
		StringBuilder sb = new StringBuilder(this.toString(key));
		sb.append(action);
		sb.append(String.format("，来自线程[id: %-3d %-15s%s", thread.getId(), thread.getState(), thread.getName()));				
		sb.append("]：");
		StackTraceElement[] stack = Thread.getAllStackTraces().get(thread);
		if (stack != null) {
			for (StackTraceElement s : stack) 
				sb.append("\n\t    at " + s);	
		}
		return sb.toString();
	}

	@Override
	public Object borrowObject(Object key) throws Exception {
		if (logger.isDebugEnabled()) 					
			logger.debug(getLoggerMessage(key, "获取对象"));					
		return pool.borrowObject(key);
	}

	@Override
	public void clear() throws Exception, UnsupportedOperationException {
		if (logger.isDebugEnabled()) 					
			logger.debug(getLoggerMessage(null, "清理对象"));					
		pool.clear();
	}

	@Override
	public void clear(Object key) throws Exception, UnsupportedOperationException {
		if (logger.isDebugEnabled()) 					
			logger.debug(getLoggerMessage(key, "清理对象"));								
		pool.clear(key);
	}

	@Override
	public void close() throws Exception {
		pool.close();
	}

	@Override
	public int getNumActive() throws UnsupportedOperationException {
		return pool.getNumActive();
	}

	@Override
	public int getNumActive(Object arg0) throws UnsupportedOperationException {
		return pool.getNumActive(arg0);
	}

	@Override
	public int getNumIdle() throws UnsupportedOperationException {
		return pool.getNumIdle();
	}

	@Override
	public int getNumIdle(Object arg0) throws UnsupportedOperationException {
		return pool.getNumIdle(arg0);
	}

	@Override
	public void invalidateObject(Object key, Object arg1) throws Exception {
		if (logger.isDebugEnabled()) 					
			logger.debug(getLoggerMessage(key, "标记无效"));			
		pool.invalidateObject(key, arg1);
	}

	@Override
	public void returnObject(Object key, Object arg1) throws Exception {
		if (logger.isDebugEnabled()) 					
			logger.debug(getLoggerMessage(key, "归还对象"));			
		pool.returnObject(key, arg1);
	}

	@Override
	public void setFactory(KeyedPoolableObjectFactory arg0) throws IllegalStateException, UnsupportedOperationException {
		pool.setFactory(arg0);
	}
	
	public String toString(Object key) {
		if (key == null)
			return String.format("资源池[%s acitve: %d idle: %d]", id, getNumActive(), getNumIdle());
		else
			return String.format("资源池[%s key: %s acitve: %d idle: %d]", id, key, getNumActive(key), getNumIdle(key));
	}
}
