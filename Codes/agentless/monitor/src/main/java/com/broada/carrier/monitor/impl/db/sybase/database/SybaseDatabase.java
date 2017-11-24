package com.broada.carrier.monitor.impl.db.sybase.database;

import java.text.NumberFormat;

/**
 * 
 * @author lixy (lixy@broada.com.cn) Create By 2007-12-7 下午06:00:37
 */
public class SybaseDatabase {

  // Sybase数据库名称
  private String dbName;

  // 数据库大小
  private double dbSize;

  // 保留大小
  private double reserved;

  // 未用大小
  private double unused;
  
  // 已使用大小
  private double usedSize;
  
  // 使用率阈值
  private double maxUsedRate = 80;
  
  // 是否监测
  private boolean isWacthed = false;
  
  private double dataSize;
  
  private double idxSize;
  
  private static NumberFormat formatter = NumberFormat.getInstance();
  
  static{
    formatter.setMaximumFractionDigits(2);
  }

  /**
   * @return dbName
   */
  public String getDbName() {
    return dbName;
  }

  /**
   * @param dbName
   */
  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  /**
   * @return dbSize
   */
  public double getDbSize() {
    return dbSize;
  }

  /**
   * @param dbSize
   */
  public void setDbSize(double dbSize) {
    this.dbSize = dbSize;
  }

  /**
   * @return reserved
   */
  public double getReserved() {
    return reserved;
  }

  /**
   * @param reserved
   */
  public void setReserved(double reserved) {
    this.reserved = reserved;
  }

  /**
   * @return unused
   */
  public double getUnused() {
    return unused;
  }

  /**
   * @param unused
   */
  public void setUnused(double unused) {
    this.unused = unused;
  }

  /**
   * @return usedRate
   */
  public double getUsedRate() {
    if (this.dbSize == 0) {
      return 0;
    }
    return new Double(formatter.format(getUsedSize()*100 / dbSize)).doubleValue();
  }

  /**
   * @return isWonted
   */
  public boolean isWacthed() {
    return isWacthed;
  }

  /**
   * @param isWonted
   */
  public void setWacthed(boolean isWacthed) {
    this.isWacthed = isWacthed;
  }

  /**
   * @return usedSize
   */
  public double getUsedSize() {
    if (usedSize == 0)
      usedSize = reserved - unused;
    return usedSize;
  }

  /**
   * @param usedSize
   */
  public void setUsedSize(double usedSize) {
    this.usedSize = usedSize;
  }

  /**
   * @return maxUsedRate
   */
  public double getMaxUsedRate() {
    return maxUsedRate;
  }

  /**
   * @param maxUsedRate
   */
  public void setMaxUsedRate(double maxUsedRate) {
    this.maxUsedRate = maxUsedRate;
  }

  /**
   * @return dataSize
   */
  public double getDataSize() {
    return dataSize;
  }

  /**
   * @param dataSize
   */
  public void setDataSize(double dataSize) {
    this.dataSize = dataSize;
  }

  /**
   * @return idxSize
   */
  public double getIdxSize() {
    return idxSize;
  }

  /**
   * @param idxSize
   */
  public void setIdxSize(double idxSize) {
    this.idxSize = idxSize;
  }

}
