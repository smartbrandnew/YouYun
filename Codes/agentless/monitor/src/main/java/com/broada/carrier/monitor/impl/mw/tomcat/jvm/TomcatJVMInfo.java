package com.broada.carrier.monitor.impl.mw.tomcat.jvm;

import com.broada.carrier.monitor.impl.mw.tomcat.Tomcat;

/**
 * Tomcat JVM信息
 */
public class TomcatJVMInfo implements Tomcat {
  private long free;
  private long total;
  private long max;
  public long getFree() {
    return free;
  }
  public void setFree(long free) {
    this.free = free;
  }
  public long getMax() {
    return max;
  }
  public void setMax(long max) {
    this.max = max;
  }
  public long getTotal() {
    return total;
  }
  public void setTotal(long total) {
    this.total = total;
  }
  public long getUsed(){
    return total-free;
  }
}
