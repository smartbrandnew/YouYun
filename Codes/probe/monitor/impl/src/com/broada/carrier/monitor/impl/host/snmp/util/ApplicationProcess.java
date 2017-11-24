package com.broada.carrier.monitor.impl.host.snmp.util;

import com.broada.carrier.monitor.server.api.entity.MonitorInstance;

public class ApplicationProcess extends MonitorInstance {
	private static final long serialVersionUID = 1L;

	private Boolean isWacthed = Boolean.FALSE;

  private int runStatus = SnmpProcessManager.RUNSTATUS_INVALID;//进程运行状态，默认为无效

  private int lastVal = 0;//上一次获取的进程CPU使用时间

  private long lastMonitorTime = 0;//上一次监测的时间

  private Double procMemPercent = new Double(0);//进程占用的内存百分比（单位%)

  private Double procMemPVavel = new Double(90); //进程占用的内存百分比阈值（单位%)

  private Double procCPUPercent = new Double(0);//进程占用的CPU百分比（单位%）

  private Double procCPUPVavel = new Double(90); //进程占用的CPU百分比阈值（单位%)

  private double procMem = 0;//进程占用的内存使用量(单位M)

  public double getProcMem() {
    return procMem;
  }

  public void setProcMem(double procMem) {
    this.procMem = procMem;
  }

  public Boolean getIsWacthed() {
    return isWacthed;
  }

  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }

  public Double getProcCPUPercent() {
    return procCPUPercent;
  }

  public void setProcCPUPercent(Double procCPUPercent) {
    this.procCPUPercent = procCPUPercent;
  }

  public Double getProcMemPercent() {
    return procMemPercent;
  }

  public void setProcMemPercent(Double procMemPercent) {
    this.procMemPercent = procMemPercent;
  }

  public int getRunStatus() {
    return runStatus;
  }

  public void setRunStatus(int runStatus) {
    this.runStatus = runStatus;
  }

  public Double getProcCPUPVavel() {
    return procCPUPVavel;
  }

  public void setProcCPUPVavel(Double procCPUPVavel) {
    this.procCPUPVavel = procCPUPVavel;
  }

  public Double getProcMemPVavel() {
    return procMemPVavel;
  }

  public void setProcMemPVavel(Double procMemPVavel) {
    this.procMemPVavel = procMemPVavel;
  }

  public long getLastMonitorTime() {
    return lastMonitorTime;
  }

  public void setLastMonitorTime(long lastMonitorTime) {
    this.lastMonitorTime = lastMonitorTime;
  }

  public int getLastVal() {
    return lastVal;
  }

  public void setLastVal(int lastVal) {
    this.lastVal = lastVal;
  }
}
