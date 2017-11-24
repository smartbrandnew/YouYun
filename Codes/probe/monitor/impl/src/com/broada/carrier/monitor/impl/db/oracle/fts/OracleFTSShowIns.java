package com.broada.carrier.monitor.impl.db.oracle.fts;

/**
 * Oracle 全表扫描显示实体类
 * 
 * @author lvhs (lvhs@broada.com.cn)
 * Create By 2008-10-11 下午02:52:40
 */
public class OracleFTSShowIns {
  public static final String[] keys = { "LONG_TABLE_SCANS_RATIO", "ROW_SOURCE_RATIO" };

  public static final String[] names = { "长表全表扫描百分比", "行源百分比" };
  
  public static final String[] comType = { "小于等于", "小于等于"};// 条件文本，显示正常条件

  public static final Double[] gateValues = new Double[]{ new Double(20.00), new Double(15.00) };

  public static final String[] units = { "%", "%" };
  
  private String name;
  
  private String value;
  
  private double gateValue;
  
  private String unit;

  public double getGateValue() {
    return gateValue;
  }

  public void setGateValue(double gateValue) {
    this.gateValue = gateValue;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
  
  public static String getNameByKey(String key){
    if("LONG_TABLE_SCANS_RATIO".equals(key)){
      return names[0];
    }else if("ROW_SOURCE_RATIO".equals(key)){
      return names[1];
    }
    return "";
  }

}
