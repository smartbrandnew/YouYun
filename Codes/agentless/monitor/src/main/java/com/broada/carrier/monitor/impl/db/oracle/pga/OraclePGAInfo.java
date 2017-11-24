package com.broada.carrier.monitor.impl.db.oracle.pga;

import com.broada.utils.Condition;

public class OraclePGAInfo {
  private Boolean isWacthed = Boolean.FALSE;

  private String name;

  private int itemIdx;

  private double currValue;

  private double thresholdValue = 100d;

  private int type = Condition.GREATERTHAN;

  private String unit;

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
}
