package com.broada.carrier.monitor.impl.mw.weblogic.snmp.wlec;

import com.broada.carrier.monitor.method.common.MonitorCondition;

public class WlsWlecInstanceInfo extends MonitorCondition {
	private static final long serialVersionUID = 1L;

	/** 对象名称 */
  private String ObjectName;

  /** 客户端连接类型 */
  private String wlecType;

  /** 客户端连接名称 */
  private String wlecName;

  /** 客户端连接地址 */
  private String address;

  /** 客户端连接请求数 */
  private long requestCount;

  /** 客户端请求等待数 */
  private long pendingCount;

  /** 客户端请求错误数 */
  private long errorCount;

  /* 阈值 */
  private long maxRequestCount = 10;

  private long maxPendingCount = 10;

  private long maxErrorCount = 10;

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public long getErrorCount() {
    return errorCount;
  }

  public void setErrorCount(long errorCount) {
    this.errorCount = errorCount;
  }

  public String getWlecName() {
    return wlecName;
  }

  public void setWlecName(String wlecName) {
    this.wlecName = wlecName;
  }

  public String getObjectName() {
    return ObjectName;
  }

  public void setObjectName(String objectName) {
    ObjectName = objectName;
  }

  public long getPendingCount() {
    return pendingCount;
  }

  public void setPendingCount(long pendingCount) {
    this.pendingCount = pendingCount;
  }

  public long getRequestCount() {
    return requestCount;
  }

  public void setRequestCount(long requestCount) {
    this.requestCount = requestCount;
  }

  public String getWlecType() {
    return wlecType;
  }

  public void setWlecType(String wlecType) {
    this.wlecType = wlecType;
  }

  public long getMaxErrorCount() {
    return maxErrorCount;
  }

  public void setMaxErrorCount(long maxErrorCount) {
    this.maxErrorCount = maxErrorCount;
  }

  public long getMaxPendingCount() {
    return maxPendingCount;
  }

  public void setMaxPendingCount(long maxPendingCount) {
    this.maxPendingCount = maxPendingCount;
  }

  public long getMaxRequestCount() {
    return maxRequestCount;
  }

  public void setMaxRequestCount(long maxRequestCount) {
    this.maxRequestCount = maxRequestCount;
  }

}
