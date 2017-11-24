package com.broada.carrier.monitor.impl.stdsvc.tcp;

import com.broada.carrier.monitor.method.common.MonitorCondition;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * TCP端口监测的条件
 * 
 * @author zhoucy (zhoucy@broada.com.cn)
 * Create By 2006-5-23 下午02:32:59
 */
public class TCPMonitorCondition extends MonitorCondition {
  private static final long serialVersionUID = 7968128260154584796L;

  // TCP 监测端口
  private int port;

  // 延时
  private int timeout;

  // 失败重试次数
  private int times;

  // 正常条件 1为端口up,0为端口down
  // private int value;

  public TCPMonitorCondition() {
    type = 0;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
    this.field = "" + port;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public int getTimes() {
    return times;
  }

  public void setTimes(int times) {
    this.times = times;
  }

  @JsonIgnore
  public boolean isUp() {
    return value.equals("1");
  }
}
