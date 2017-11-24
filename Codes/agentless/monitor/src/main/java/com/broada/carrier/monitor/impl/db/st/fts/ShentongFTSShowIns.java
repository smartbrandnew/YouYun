package com.broada.carrier.monitor.impl.db.st.fts;

/**
 * shentong全表扫描显示实体类
 * 
 * @author Zhouqa
 * Create By 2016年4月13日 上午10:00:36
 */
public class ShentongFTSShowIns {
  public static final String[] keys = { "ROW_SOURCE_RATIO" };

  public static final String[] names = { "行源百分比" };
  
  public static final String[] comType = { "小于等于"};// 条件文本，显示正常条件

  public static final Double[] gateValues = new Double[]{ new Double(15.00) };

  public static final String[] units = { "%" };
  
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
  	if("ROW_SOURCE_RATIO".equals(key)){
      return names[0];
    }
    return "";
  }

}
