package com.broada.carrier.monitor.impl.host.cli.disk;

/**
 * @author panlx
 *		panlx@broada.com
 */
public class DirSpaceInfo {

  private double dirSize;        //目录大小（单位：字节）
  private double dirUsedRate;    //目录使用率（单位：%）
  
  public double getDirSize() {
    return dirSize;
  }
  public void setDirSize(double dirSize) {
    this.dirSize = dirSize;
  }
  public double getDirUsedRate() {
    return dirUsedRate;
  }
  public void setDirUsedRate(double dirUsedRate) {
    this.dirUsedRate = dirUsedRate;
  }
  
}
