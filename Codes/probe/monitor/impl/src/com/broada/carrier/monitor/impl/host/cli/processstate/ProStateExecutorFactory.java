package com.broada.carrier.monitor.impl.host.cli.processstate;

import java.util.HashMap;
import java.util.Map;

public class ProStateExecutorFactory {
  private static final Map executorCache = new HashMap();

  public static ProStateExecutor getProcessExecutor(String srvId) {
    ProStateExecutor executor = (ProStateExecutor) executorCache.get("" + srvId);
    if (executor == null) {
      executor = new ProStateExecutor();
      if ("-1".equals(srvId)) {//srvId为-1的不应该放入缓存,否则配置的时候,取总内存会出错
        executorCache.put("" + srvId, executor);
      }
    }
    return executor;
  }
}
