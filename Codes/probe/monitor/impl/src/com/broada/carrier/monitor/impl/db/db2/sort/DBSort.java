package com.broada.carrier.monitor.impl.db.db2.sort;

/**
 * DB2排序监测实体类
 * @author 杨帆
 * 
 */
public class DBSort {
  int totalSorts;

  double sortOverRatio;

  public double getSortOverRatio() {
    return sortOverRatio;
  }

  public void setSortOverRatio(double sortOverRatio) {
    this.sortOverRatio = sortOverRatio;
  }

  public int getTotalSorts() {
    return totalSorts;
  }

  public void setTotalSorts(int totalSorts) {
    this.totalSorts = totalSorts;
  }
}
