package com.broada.carrier.monitor.impl.db.oracle.redolog;

import com.broada.utils.Condition;

/**
 * Oracle Redo 日志
 * 
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2008-10-16 下午02:37:05
 */
public class RedoLogInfo {

  private Boolean isWacthed = Boolean.FALSE;

  private String name;

  private int itemIdx;

  private double currValue;

  private double thresholdValue = 100d;

  private int type = Condition.LESSTHAN;

  private String unit;
  //该项是否在面板中显示
  private Boolean isShowInColumn = Boolean.TRUE;

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public Boolean getIsWacthed() {
    return isWacthed;
  }

  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }

  public String getName() {
    return name;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getCurrValue() {
    return currValue;
  }

  public void setCurrValue(double currValue) {
    this.currValue = currValue;
  }

  public double getThresholdValue() {
    return thresholdValue;
  }

  public void setThresholdValue(double thresholdValue) {
    this.thresholdValue = thresholdValue;
  }

  public int getItemIdx() {
    return itemIdx;
  }

  public void setItemIdx(int itemIdx) {
    this.itemIdx = itemIdx;
  }

  public Boolean getIsShowInColumn() {
    return isShowInColumn;
  }

  public void setIsShowInColumn(Boolean isShowInColumn) {
    this.isShowInColumn = isShowInColumn;
  }

}
///*
//* 初始以Willing-to-wait请求类型请求一个latch不成功的总次数
//*/
//private Integer total_mis;
///*
//* 成功地以Willing-to-wait请求类型请求一个latch的总次数
//*/
//private Integer total_gets;
///*
//* 以Immediate请求类型请求一个latch不成功的总次数 
//*/
//private Integer total_imm_mis;
///*
//* 以Immediate请求类型成功地获得一个latch的总次数
//*/
//private Integer total_imm_gets;
///*
//* Willing-to-wait请求类型的丢失量占其获得数的百分比
//*/
//private double willing_to_wait_ratio;
///*
//* Immediate请求类型的丢失量占其获得数的百分比
//*/
//private double immidiate_ratio;
///*
//* 重做日志缓冲中用户进程不能分配空间的次数
//*/
//private Integer value;
///*
//* 归档重做日志文件的数目
//*/
//private Integer counts = new Integer(100);
///*
//* 重做条目的平均大小
//*/
//private double aveSize;
