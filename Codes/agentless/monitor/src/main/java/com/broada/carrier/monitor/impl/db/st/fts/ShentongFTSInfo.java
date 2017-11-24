package com.broada.carrier.monitor.impl.db.st.fts;

/**
 * Shentong全表扫描指标信息实体类
 * 
 * @author Zhouqa
 * Create By 2016年4月13日 上午9:58:18
 */
public class ShentongFTSInfo {
  
  private long ftsRows;// 全表扫描获得的行数
  
  private long fbiRows;// 从行标识获得的行数

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

}
