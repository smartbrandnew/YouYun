package com.broada.carrier.monitor.method.cli.pool;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.method.cli.CLICollector;

public class CollectorCacheEventListener implements CacheEventListener {
  private final static Log logger = LogFactory.getLog(CollectorCacheEventListener.class);

  public void dispose() {
  }

  public void notifyElementEvicted(Ehcache cache, Element element) {
    closeSession(element);
  }

  public void notifyElementExpired(Ehcache cache, Element element) {
    closeSession(element);
  }

  public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
  }

  public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
    closeSession(element);
  }

  public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
  }

  public void notifyRemoveAll(Ehcache cache) {
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  private void closeSession(Element element) {
    Object obj = element.getObjectValue();
    logger.debug("缓存失效或者被删除,即将关闭CLI连接。对象：" + obj);

    if (obj instanceof CLICollector) {    	
    	if (!((CLICollector) obj).isClosed()) {	      
	      CLICollector cliCollector = (CLICollector) obj;
	      cliCollector.destroy();
    	}
    }
  }
}
