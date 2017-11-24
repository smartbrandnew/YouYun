package com.broada.carrier.monitor.impl.db.oracle.undostat;

public class OracleUndostatShowIns {
  public static String[] keys ={"UNDOSTAT_NOSPACEERRCNT","UNDOSTAT_SSOLDERRCNT"};
  public static String[] names = {"无空间错误计数","快照太旧错误计数"};
  public static Integer[] gateValues = {new Integer(5),new Integer(5)};
  public static String[] units = {"个","个"};
  /* 监测项目名 */
  String name;

  /* 监测项目实际值 */
  int value;

  /* 监测比较类型 */
  String comType;

  /* 监测项目阈值 */
  Integer gateValue = new Integer(0);

  /* 监测项目单位 */
  String unit;

  public String getComType() {
    return comType;
  }

  public void setComType(String comType) {
    this.comType = comType;
  }

  public Integer getGateValue() {
    return gateValue;
  }

  public void setGateValue(Integer gateValue) {
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

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

}
