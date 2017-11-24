package com.broada.carrier.monitor.impl.mw.weblogic.snmp.thread;

import com.broada.carrier.monitor.impl.common.StringUtil;
import com.broada.carrier.monitor.method.common.MonitorCondition;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class WLSThreadInfo extends MonitorCondition {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 4715509993535143677L;

  private String totalThreadNumber = "0";

  private String idleThreadNumber;

  private String queueLength = "0";

  private String pendingRequestNumber;

  public String getPendingRequestNumber() {
    return pendingRequestNumber;
  }

  public void setPendingRequestNumber(String pendingRequestNumber) {
    this.pendingRequestNumber = pendingRequestNumber;
  }

  public WLSThreadInfo() {
  }

  public WLSThreadInfo(String field) {
    this.field = field;
  }

  public String getIdleThreadNumber() {
    return idleThreadNumber;
  }

  public String getQueueLength() {
    return queueLength;
  }



  public String getTotalThreadNumber() {
    return totalThreadNumber;
  }



  public void setQueueLength(String queueLength) {
    this.queueLength = queueLength;
  }

  public void setIdleThreadNumber(String idleThreadNumber) {
    this.idleThreadNumber = idleThreadNumber;
  }

  public void setTotalThreadNumber(String totalThreadNumber) {
    this.totalThreadNumber = totalThreadNumber;
  }

  public String getValue() {
    return StringUtil.convertNull2Blank(value);
  }

  public String toString() {
    return getField() + ", 总线程:" + getTotalThreadNumber() + "个,空闲线程:" + getIdleThreadNumber() + "个,当前等待请求个数:"
        + getPendingRequestNumber();
  }
}
