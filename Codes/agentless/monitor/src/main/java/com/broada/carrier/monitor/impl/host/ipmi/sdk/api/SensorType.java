package com.broada.carrier.monitor.impl.host.ipmi.sdk.api;

import java.io.Serializable;


/**
 * 传感器类型
 * 
 * @author pippo 
 * Create By 2013-7-30 下午7:00:29
 */
public enum SensorType implements Serializable{
	
  TEM("℃", "degrees C"), 
	
  VOL("V", "Volts"), 

  CUR("A", "Amps"),
  
  FAN("RPM", "RPM"),
  
  POW("W", "Watts");
	
  private String value;
  private String label;

  public static SensorType parseFromValue(String value)
  {
    for (SensorType type : values()) {
      if (type.getValue() == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型：" + value);
  }

  private SensorType(String label, String value) {
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
