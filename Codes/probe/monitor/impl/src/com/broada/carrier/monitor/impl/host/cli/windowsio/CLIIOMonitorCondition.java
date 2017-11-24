package com.broada.carrier.monitor.impl.host.cli.windowsio;

import com.broada.carrier.monitor.method.common.MonitorCondition;

/**
 * windows磁盘I/O监测条件
 * 
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2008-6-30 下午05:15:16
 */
public class CLIIOMonitorCondition extends MonitorCondition {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -3468797979078050045L;

  /** 等待进行磁盘访问的系统请求数量 */
  private int currentDiskQueueLength;

  /** 写速率(KB/S) */
  private float diskWriteBytesPerSec;

  /** 读速率(KB/S) */
  private float diskReadBytesPerSec;

  /** 读操作次数/每秒 */
  private float diskReadsPerSec;

  /** 写操作次数/每秒 */
  private float diskWritesPerSec;

  /** 读请求操作所占的时间百分比 */
  private float percentDiskReadTime;

  /** 磁盘忙于读/写活动所用时间的百分比 */
  private float PercentDiskTime;

  /** 写请求操作所占的时间百分比 */
  private float percentDiskWriteTime;

  /** 盘空闲所占用的百分比 */
  private float percentIdleTime;

  /** 读速率阈值(KB/S) */
  private float maxReadBytesPerSec = 1024;

  /** 写速率阈值(KB/S) */
  private float maxWriteBytesPerSec = 1024;

  public CLIIOMonitorCondition() {
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof CLIIOMonitorCondition)) {
      return false;
    }
    return getField().equals(((CLIIOMonitorCondition) obj).getField());
  }

  public String getComDescription() {
    return "";
  }

  public void setField(String field) {
    super.setField(field);
  }

  public String getFieldCondition() {
    return "磁盘的I/O读写速率";
  }

  public int getCurrentDiskQueueLength() {
    return currentDiskQueueLength;
  }

  public void setCurrentDiskQueueLength(int currentDiskQueueLength) {
    this.currentDiskQueueLength = currentDiskQueueLength;
  }

  public float getDiskReadBytesPerSec() {
    return diskReadBytesPerSec;
  }

  public void setDiskReadBytesPerSec(float diskReadBytesPerSec) {
    this.diskReadBytesPerSec = diskReadBytesPerSec;
  }

  public float getDiskReadsPerSec() {
    return diskReadsPerSec;
  }

  public void setDiskReadsPerSec(float diskReadsPerSec) {
    this.diskReadsPerSec = diskReadsPerSec;
  }

  public float getDiskWriteBytesPerSec() {
    return diskWriteBytesPerSec;
  }

  public void setDiskWriteBytesPerSec(float diskWriteBytesPerSec) {
    this.diskWriteBytesPerSec = diskWriteBytesPerSec;
  }

  public float getDiskWritesPerSec() {
    return diskWritesPerSec;
  }

  public void setDiskWritesPerSec(float diskWritesPerSec) {
    this.diskWritesPerSec = diskWritesPerSec;
  }

  public float getMaxReadBytesPerSec() {
    return maxReadBytesPerSec;
  }

  public void setMaxReadBytesPerSec(float maxReadBytesPerSec) {
    this.maxReadBytesPerSec = maxReadBytesPerSec;
  }

  public float getMaxWriteBytesPerSec() {
    return maxWriteBytesPerSec;
  }

  public void setMaxWriteBytesPerSec(float maxWriteBytesPerSec) {
    this.maxWriteBytesPerSec = maxWriteBytesPerSec;
  }

  public float getPercentDiskReadTime() {
    return percentDiskReadTime;
  }

  public void setPercentDiskReadTime(float percentDiskReadTime) {
    this.percentDiskReadTime = percentDiskReadTime;
  }

  public float getPercentDiskTime() {
    return PercentDiskTime;
  }

  public void setPercentDiskTime(float percentDiskTime) {
    PercentDiskTime = percentDiskTime;
  }

  public float getPercentDiskWriteTime() {
    return percentDiskWriteTime;
  }

  public void setPercentDiskWriteTime(float percentDiskWriteTime) {
    this.percentDiskWriteTime = percentDiskWriteTime;
  }

  public float getPercentIdleTime() {
    return percentIdleTime;
  }

  public void setPercentIdleTime(float percentIdleTime) {
    this.percentIdleTime = percentIdleTime;
  }
}
