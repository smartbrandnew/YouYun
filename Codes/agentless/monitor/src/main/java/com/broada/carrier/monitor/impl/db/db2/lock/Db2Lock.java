package com.broada.carrier.monitor.impl.db.db2.lock;

public class Db2Lock {
  /**
   * 死锁数
   */
  private Double deadLockCnt = new Double(0);
  
  /**
   * 锁升级数
   */
  private Double escalLockCnt = new Double(0);
  
  /**
   * 等待锁定的应用数
   */
  private Double waitingLockCnt = new Double(0);
  
  /**
   * 当前连接的应用数
   */
  private Double currAppCnt = new Double(0);
  
  /**
   * 锁升级率
   */
  private Double escalLockRatio = new Double(0);
  
  /**
   * 数据获取时间
   */
  private long time;

  public Double getCurrAppCnt() {
    return currAppCnt;
  }

  public void setCurrAppCnt(Double currAppCnt) {
    this.currAppCnt = currAppCnt;
  }

  public Double getDeadLockCnt() {
    return deadLockCnt;
  }

  public void setDeadLockCnt(Double deadLockCnt) {
    this.deadLockCnt = deadLockCnt;
  }

  public Double getEscalLockCnt() {
    return escalLockCnt;
  }

  public void setEscalLockCnt(Double escalLockCnt) {
    this.escalLockCnt = escalLockCnt;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public Double getWaitingLockCnt() {
    return waitingLockCnt;
  }

  public void setWaitingLockCnt(Double waitingLockCnt) {
    this.waitingLockCnt = waitingLockCnt;
  }

  /**
   * 等待锁定的应用程序所占的百分比
   */
  public Double getAppWaitingRatio() {
    return getCurrAppCnt()==0?0:getWaitingLockCnt()/getCurrAppCnt();
  }

  public Double getEscalLockRatio() {
    return escalLockRatio;
  }

  public void setEscalLockRatio(Double escalLockRatio) {
    this.escalLockRatio = escalLockRatio;
  }
}
