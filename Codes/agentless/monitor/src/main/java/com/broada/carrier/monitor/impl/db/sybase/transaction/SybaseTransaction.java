package com.broada.carrier.monitor.impl.db.sybase.transaction;

/**
 * Sybase Transaction 实体类
 * 
 * @author chenmw
 * Create By 2008-12-4 下午02:08:13
 */
public class SybaseTransaction {
  public static final String[] FIELDS = new String[] { "total", "sumpec", "abort" };

  public static final String[] TRANSACTIONTYPES = new String[] { "系统事务交易总数", "每秒事务数", "失败的事务数" };

  public static final int[] THRESHOLDS = new int[] { new Integer(1000), new Integer(50), new Integer(5) };

  public static final String[] UNITS = new String[] { "个", "个/秒", "个/秒" };

  public SybaseTransaction(String filed) {
    this.field = filed;
  }

  //事务类型标识
  private String field;

  //事务类型描述
  private String transactionType;

  //每秒事务数
  private double transactionNumPerSec;

  //阈值
  private int thresHold;

  //单位
  private String unit;

  //比较类型
  private String compareType;

  //是否监测
  private boolean isWacthed = false;

  public boolean isWacthed() {
    return isWacthed;
  }

  public void setWacthed(boolean isWacthed) {
    this.isWacthed = isWacthed;
  }

  public int getThresHold() {
    return thresHold;
  }

  public void setThresHold(int thresHold) {
    this.thresHold = thresHold;
  }

  public double getTransactionNumPerSec() {
    return transactionNumPerSec;
  }

  public void setTransactionNumPerSec(double transactionNumPerSec) {
    this.transactionNumPerSec = transactionNumPerSec;
  }

  public String getTransactionType() {
    return transactionType;
  }

  public void setTransactionType(String transactionType) {
    this.transactionType = transactionType;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public String getCompareType() {
    return compareType;
  }

  public void setCompareType(String compareType) {
    this.compareType = compareType;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }
}
