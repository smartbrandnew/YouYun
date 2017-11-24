package com.broada.carrier.monitor.impl.db.oracle.roll;

import java.io.Serializable;

import com.broada.carrier.monitor.impl.common.PerfItem;

/**
 * Oracle 回滚段模型
 * 
 * @author Lixy (lixy@broada.com.cn)
 * Create By 2006-10-25 11:20:06
 */
public class OracleRollback implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String ITEM_RSSIZE = "ORACLE-ROLL-1";
  public static final String ITEM_HITRATE = "ORACLE-ROLL-2";

	private String[] alermItem = new String[] { "size", "hitRate" }; //告警项(回滚段大小、命中率)

  private String rollName; //回滚段名称

  private double rssize = 0; //回滚段默认（当前）大小

  private double maxRssize = 2048; //回滚段大小阈值

  private int shrinkCount = 0; //回滚段收缩次数
  private double aveShrink = 0;
  
  private int wrapCount = 0; //回滚段翻转次数
  
  private int extendCount = 0;  //回滚段扩展次数
  
  private int xactCount = 0;  //活动事务数
  
  private int getCount = 0; //获取回滚段头次数
  
  private int waitCount = 0; //回滚段头等待次数
  
  private double hitRate = 100; //回滚段命中率
  
  private double lowHitRate = 95; //回滚段命中率阈值

  private String statusStr = "ONLINE"; //状态描述
  
  private Boolean isWacthed = Boolean.FALSE;

  public void setRollName(String rollName) {
    this.rollName = rollName;
  }

  public void setRssize(double currSize) {
    this.rssize = currSize;
  }

  public void setMaxRssize(double maxSize) {
    this.maxRssize = maxSize;
  }

  public void setHitRate(double hitRate) {
    this.hitRate = hitRate;
  }

  public void setLowHitRate(double lowHitRate) {
    this.lowHitRate = lowHitRate;
  }

  public void setShrinkCount(int shrinks) {
    this.shrinkCount = shrinks;
  }

  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }

  public String getRollName() {
    return rollName;
  }

	@PerfItem(code = ITEM_RSSIZE, name = "回滚段大小", unit="MB")
	public double getRssize() {
		return rssize;
	}
	
  public double getMaxRssize() {
    return maxRssize;
  }

  @PerfItem(code = ITEM_HITRATE, name = "命中率", unit="%")
  public double getHitRate() {
    return hitRate;
  }

  public double getLowHitRate() {
    return lowHitRate;
  }

  @PerfItem(code = "ORACLE-ROLL-3", name = "收缩次数", unit="次")
  public int getShrinkCount() {
    return shrinkCount;
  }

  public Boolean getIsWacthed() {
    return isWacthed;
  }

  /**
   * 取得大小监测条件key名称
   * @return
   */
  public String getSizeConditionName() {
    return rollName + alermItem[0];
  }

  /**
   * 取得命中率监测条件key名称
   * @return
   */
  public String getHistRateConditionName() {
    return rollName + alermItem[1];
  }

  @PerfItem(code = "ORACLE-ROLL-4", name = "等待率", unit="%")
  public double getWaitRate() {
    return 100 - this.getHitRate();
  }

  @PerfItem(code = "ORACLE-ROLL-8", name = "翻转次数", unit="次")
  public int getWrapCount() {
    return wrapCount;
  }

  public void setWrapCount(int wrapsCount) {
    this.wrapCount = wrapsCount;
  }

  @PerfItem(code = "ORACLE-ROLL-7", name = "扩展次数", unit="次")
  public int getExtendCount() {
    return extendCount;
  }

  public void setExtendCount(int extendCount) {
    this.extendCount = extendCount;
  }

  @PerfItem(code = "ORACLE-ROLL-9", name = "活动事务数", unit="个")
  public int getXactCount() {
    return xactCount;
  }

  public void setXactCount(int xactCount) {
    this.xactCount = xactCount;
  }

  @PerfItem(code = "ORACLE-ROLL-6", name = "获取次数", unit="次")
  public int getGetCount() {
    return getCount;
  }

  public void setGetCount(int getCount) {
    this.getCount = getCount;
  }

  @PerfItem(code = "ORACLE-ROLL-10", name = "等待次数", unit="次")
  public int getWaitCount() {
    return waitCount;
  }

  public void setWaitCount(int waitCount) {
    this.waitCount = waitCount;
  }

  public String getStatusStr() {
    return statusStr;
  }
  
  public void setStatusStr(String statusStr) {
    this.statusStr = statusStr;
  }

  @PerfItem(code = "ORACLE-ROLL-5", name = "平均收缩大小", unit="MB")
	public double getAveShrink() {
		return aveShrink;
	}

	public void setAveShrink(double aveShrink) {
		this.aveShrink = aveShrink;
	}
}
