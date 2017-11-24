package com.broada.carrier.monitor.test;

import com.broada.component.utils.runcheck.jmx.JmxJvmInfoProviderMBean;
import com.broada.component.utils.runcheck.jmx.MBeanRegistry;
import com.broada.carrier.monitor.test.JvmInfoProvider;

public class JmxJvmInfoProvider implements JmxJvmInfoProviderMBean {
  private JvmInfoProvider target;

  public JmxJvmInfoProvider(JvmInfoProvider target) {
    this.target = target;
    MBeanRegistry.registry(this, target.getClass().getSimpleName());
  }

  public int getThreadCount() {
    return JvmInfoProvider.getThreadCount();
  }

  public String dumpThreads() {
    return JvmInfoProvider.dumpThreads(true);
  }

  public double getHeapUsage() {
    return JvmInfoProvider.getHeapUsage();
  }

  public long getNonHeapUsed() {
    return JvmInfoProvider.getNonHeapUsed();
  }

  public long getNonHeapSize() {
    return JvmInfoProvider.getNonHeapSize();
  }

  public double getNonHeapUsage() {
    return JvmInfoProvider.getNonHeapUsage();
  }

  public long getHeapUsed() {
    return JvmInfoProvider.getHeapUsed();
  }

  public long getHeapSize() {
    return JvmInfoProvider.getHeapSize();
  }

  public long getMaxHeapUsed() {
    return this.target.getMaxHeapUsed();
  }

  public void setMaxHeapUsed(long maxHeapUsed) {
    this.target.setMaxHeapUsed(maxHeapUsed);
  }

  public long getMaxNonHeapUsed() {
    return this.target.getMaxNonHeapUsed();
  }

  public void setMaxNonHeapUsed(long maxNonHeapUsed) {
    this.target.setMaxNonHeapUsed(maxNonHeapUsed);
  }

  public int getMaxThreads() {
    return this.target.getMaxThreads();
  }

  public void setMaxThreads(int maxThreads) {
    this.target.setMaxThreads(maxThreads);
  }

  public long getRunTime() {
    return this.target.getRunTime();
  }
}
