package com.broada.carrier.monitor.impl.db.db2.sort;

/**
 * DB2排序展现实体类
 * @author 杨帆
 * 
 */
public class Db2SortShowIns {
  public static final String[] keys = { "TOTAL_SORTS", "SORT_OVER_RATIO" };

  public static final String[] names = { "应用排序数", "排序溢出百分比" };

  public static final Double[] gateValues = { new Double(4000), new Double(20) };

  public static final String[] units = { "个", "%" };

  String name;

  String value;

  String comType;

  Double gateValue;

  String unit;

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public Double getGateValue() {
    return gateValue;
  }

  public void setGateValue(Double gateValue) {
    this.gateValue = gateValue;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getComType() {
    return comType;
  }

  public void setComType(String comType) {
    this.comType = comType;
  }
}
