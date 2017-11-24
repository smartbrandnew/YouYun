package com.broada.carrier.monitor.impl.host.ipmi.sdk.api;

import java.io.Serializable;


/**
 * 状态信息结果类型
 * 
 * @author pippo 
 * Create By 2013-7-30 下午7:00:29
 */
public enum HealthType implements Serializable{
	
	NORMAL("1", "正常"), 
	
	NOREAD("2", "不可读"), 
	
	FAULT("0", "故障");
	
  private String label;
  private String value;

  public static HealthType parseFromValue(String value)
  {
    for (HealthType type : values()) {
      if (type.getValue() == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型value=：" + value);
  }
  
  public static HealthType parseFromLable(String label)
  {
    for (HealthType type : values()) {
      if (type.getLabel().equalsIgnoreCase(label)) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型label=：" + label);
  }

  private HealthType(String label, String value) {
    this.label = label;
    this.value = value;
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
