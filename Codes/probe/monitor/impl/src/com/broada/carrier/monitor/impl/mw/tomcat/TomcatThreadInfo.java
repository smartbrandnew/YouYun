package com.broada.carrier.monitor.impl.mw.tomcat;

/**
 * Tomcat一个连接器下面的线程信息.
 */
public class TomcatThreadInfo implements Tomcat{
  private long maxThreads;

  private long minSpareThreads;

  private long maxSpareThreads;

  private long currentThreadCount;

  private long currentThreadsBusy;

  public long getCurrentThreadCount() {
    return currentThreadCount;
  }

  public void setCurrentThreadCount(long currentThreadCount) {
    this.currentThreadCount = currentThreadCount;
  }

  public long getCurrentThreadsBusy() {
    return currentThreadsBusy;
  }

  public void setCurrentThreadsBusy(long currentThreadsBusy) {
    this.currentThreadsBusy = currentThreadsBusy;
  }

  public long getMaxSpareThreads() {
    return maxSpareThreads;
  }

  public void setMaxSpareThreads(long maxSpareThreads) {
    this.maxSpareThreads = maxSpareThreads;
  }

  public long getMaxThreads() {
    return maxThreads;
  }

  public void setMaxThreads(long maxThreads) {
    this.maxThreads = maxThreads;
  }

  public long getMinSpareThreads() {
    return minSpareThreads;
  }

  public void setMinSpareThreads(long minSpareThreads) {
    this.minSpareThreads = minSpareThreads;
  }

}
