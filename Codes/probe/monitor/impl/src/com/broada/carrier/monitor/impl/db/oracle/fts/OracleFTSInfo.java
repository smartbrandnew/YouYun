package com.broada.carrier.monitor.impl.db.oracle.fts;

/**
 * Oracle 全表扫描指标信息实体类
 * 
 * @author lvhs (lvhs@broada.com.cn)
 * Create By 2008-10-11 下午02:50:27
 */
public class OracleFTSInfo {
  private long ltScanTimes;// 长表全表扫描次数
  
  private long stScanTimes;// 短表全表扫描次数
  
  private long ftsRows;// 全表扫描获得的行数
  
  private long fbiRows;// 从行标识获得的行数

  /**
   * 获取长表全表扫描比率
   * 
   * @return
   */
  public double getLtScanRatio() {
    long total = ltScanTimes + stScanTimes;
    if(total == 0){
      return 0.0;
    }
    return (double)ltScanTimes/total * 100;
  }

  /**
   * 获取行源比率
   * 
   * @return
   */
  public double getRsRatio() {
    long total = ftsRows + fbiRows;
    if(total == 0){
      return 0.0;
    }
    return (double)ftsRows/total * 100;
  }
  
  public void setFbiRows(long fbiRows) {
    this.fbiRows = fbiRows;
  }

  public void setFtsRows(long ftsRows) {
    this.ftsRows = ftsRows;
  }

  public void setLtScanTimes(long ltScanTimes) {
    this.ltScanTimes = ltScanTimes;
  }

  public void setStScanTimes(long stScanTimes) {
    this.stScanTimes = stScanTimes;
  }
}
