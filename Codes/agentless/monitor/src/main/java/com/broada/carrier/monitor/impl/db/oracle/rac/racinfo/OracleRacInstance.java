package com.broada.carrier.monitor.impl.db.oracle.rac.racinfo;

public class OracleRacInstance {

  private String instId;
  private String instNumber;
  private String instanceName;
  private String parallel;//并联
  private String hostName;
  private String status;  // NOMOUNT(启动不装载);MOUNT(装载数据库);OPEN(打开);SHUTDOWN(关闭)
  private String databaseStatus;
  private String activeState;
  private String startUpTime;
  private Boolean isWacthed = Boolean.TRUE;
  /*public String getInstId() {
    return instId;
  }*/
  public void setInstId(String instId) {
    this.instId = instId;
  }
  public String getInstanceName() {
    return instanceName;
  }
  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }
  public String getHostName() {
    return hostName;
  }
  public void setHostName(String hostName) {
    this.hostName = hostName;
  }
  public String getStartUpTime() {
    return startUpTime;
  }
  public void setStartUpTime(String startUpTime) {
    this.startUpTime = startUpTime;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }
  public String getActiveState() {
    return activeState;
  }
  public void setActiveState(String activeState) {
    this.activeState = activeState;
  }
  public String getInstNumber() {
    return instNumber;
  }
  public void setInstNumber(String instNumber) {
    this.instNumber = instNumber;
  }
  public String getParallel() {
    return parallel;
  }
  public void setParallel(String parallel) {
    this.parallel = parallel;
  }
  public String getDatabaseStatus() {
    return databaseStatus;
  }
  public void setDatabaseStatus(String databaseStatus) {
    this.databaseStatus = databaseStatus;
  }
  public Boolean getIsWacthed() {
    return isWacthed;
  }
  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }
  
}
