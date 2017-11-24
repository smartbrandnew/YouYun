package com.broada.carrier.monitor.impl.db.oracle.recursion;

public class OracleRecursionShowIns {
  public static String[] keys ={"RECURSION_CALL_RATIO_IN_TIMES","RECURSION_CALL_RATIO","RECURSION_CALL_VELOCITY","RECURSION_USER_RATIO"};
  public static String[] names = {"时间间隔的递归调用百分比", "递归调用百分比", "递归调用速率", "递归-用户调用比率"};
  public static Double[] gateValues = {new Double(50), new Double(50), new Double(10), new Double(1)};
  public static String[] units = {"%","%","个/秒","比率"};
  
  /* 监测项目名 */
  String name;

  /* 监测项目实际值 */
  double value;

  /* 监测比较类型 */
  String comType;

  /* 监测项目阈值 */
  Double gateValue = new Double(0);

  /* 监测项目单位 */
  String unit;

  public String getComType() {
    return comType;
  }

  public void setComType(String comType) {
    this.comType = comType;
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

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }
}
