package com.broada.carrier.monitor.impl.host.ipmi;

import java.io.Serializable;


/**
 * 指标类型
 * 
 * @author pippo 
 * Create By 2013-7-30 下午7:00:29
 */
public enum ThreshType implements Serializable{
	
  TEM("温度(℃)", "℃"), 
	
  VOL("电压(V)", "V"), 

  CUR("电流(A)", "A"),
  
  FAN("转速(RPM)", "RPM"),
  
  POW("功率(W)", "W");
	
  private String value;
  private String label;

  public static ThreshType parseFromValue(String value)
  {
    for (ThreshType type : values()) {
      if (type.getValue() == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型：" + value);
  }

  private ThreshType(String label, String value) {
    this.value = value;
    this.label = label;
  }

  public String getValue() {
    return this.value;
  }
  
  public String getLabel() {
    return this.label;
  }
  
  public String toString() {
    return this.label + "[" + this.value + "]";
  }
  
}
