package com.broada.carrier.monitor.impl.mw.weblogic.snmp.servlet;

import com.broada.carrier.monitor.method.common.MonitorCondition;

public class WLSServletInfo extends MonitorCondition {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 6701043356747729796L;

  private boolean select = false;

  private boolean exists = true;

  /*
   * 最大执行时间
   */
  private String maxTime;

  /*
   * 平均执行时间
   */
  private String avgTime;

  /*
   * 调用次数
   */
  private String invokeTimes;

  /*
   * 运行总时间
   */
  private String totalTime;

  public WLSServletInfo() {
    setValue("800");
  }

  public String getAvgTime() {
    return avgTime;
  }

  public void setAvgTime(String avgTime) {
    this.avgTime = avgTime;
  }

  public String getInvokeTimes() {
    return invokeTimes;
  }

  public void setInvokeTimes(String invokeTimes) {
    this.invokeTimes = invokeTimes;
  }

  public String getMaxTime() {
    return maxTime;
  }

  public void setMaxTime(String maxTime) {
    this.maxTime = maxTime;
  }

  public boolean isSelect() {
    return select;
  }

  public void setSelect(boolean select) {
    this.select = select;
  }

  public boolean isExists() {
    return exists;
  }

  public void setExists(boolean exists) {
    this.exists = exists;
  }

  public String getTotalTime() {
    return totalTime;
  }

  public void setTotalTime(String totalTime) {
    this.totalTime = totalTime;
  }
}
