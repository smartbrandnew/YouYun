package com.broada.carrier.monitor.impl.mw.weblogic.snmp.jca;

/**
 * weblogic JCA展现实体类
 * @author 杨帆
 * 
 */
public class JCAShowIns {
  public static final String[] keys = { "ABILITY_SCALE", "BLAB_SCALE" };

  public static final String[] names = { "可用性百分比", "连接泄漏比例" };

  public static final Double[] gateValues = { new Double(80), new Double(20) };

  public static final String[] units = { "%", "%" };

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
