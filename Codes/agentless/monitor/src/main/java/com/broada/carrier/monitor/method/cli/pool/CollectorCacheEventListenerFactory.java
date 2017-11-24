package com.broada.carrier.monitor.method.cli.pool;

import java.util.Properties;

import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;

public class CollectorCacheEventListenerFactory extends CacheEventListenerFactory {

  public CacheEventListener createCacheEventListener(Properties properties) {
    return new CollectorCacheEventListener();
  }

}
