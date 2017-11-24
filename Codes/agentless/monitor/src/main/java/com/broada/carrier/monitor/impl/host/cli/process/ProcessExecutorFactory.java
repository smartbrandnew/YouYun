package com.broada.carrier.monitor.impl.host.cli.process;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessExecutorFactory {
  private static final Map<String, ProcessExecutor> executorCache = new ConcurrentHashMap<String, ProcessExecutor>();

  public static ProcessExecutor getProcessExecutor(String srvId) {
    ProcessExecutor executor = (ProcessExecutor) executorCache.get(srvId);
    if (executor == null) {
      executor = new ProcessExecutor();
      if ("-1".equals(srvId)) {//srvId为-1的不应该放入缓存,否则配置的时候,取总内存会出错
        executorCache.put(srvId, executor);
      }
    }
    return executor;
  }
}
