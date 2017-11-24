package com.broada.carrier.monitor.test;

import com.broada.component.utils.runcheck.jmx.JmxRuntimeCheckerMBean;
import com.broada.component.utils.runcheck.jmx.MBeanRegistry;
import com.broada.carrier.monitor.test.RuntimeChecker;

public class JmxRuntimeChecker implements JmxRuntimeCheckerMBean {
  private RuntimeChecker target;

  public JmxRuntimeChecker(RuntimeChecker target) {
    this.target = target;
    MBeanRegistry.registry(this, target.getClass().getSimpleName());
  }

  public String dumpRuntimeInfo() {
    return this.target.dumpRuntimeInfo();
  }

  public long getMaxWarnDuration() {
    return this.target.getMaxWarnDuration();
  }

  public void setMaxWarnDuration(long maxWarnDuration) {
    this.target.setMaxWarnDuration(maxWarnDuration);
  }

  public int getCheckInterval() {
    return this.target.getCheckInterval();
  }

  public void setCheckInterval(int checkInterval) {
    this.target.setCheckInterval(checkInterval);
  }

  public boolean isCheckSelf() {
    return this.target.isCheckSelf();
  }

  public void setCheckSelf(boolean checkSelf) {
    this.target.setCheckSelf(checkSelf);
  }
}
