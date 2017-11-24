package com.broada.carrier.monitor.impl.host.ipmi.sdk.api;

import java.io.Serializable;


/**
 * 结果类型
 * 
 * @author pippo 
 * Create By 2013-7-30 下午7:00:29
 */
public enum ResultType implements Serializable{
	
  TRUE("是", true), 
	
  FALSE("否", false);
	
  private String label;
  private boolean value;

  public static ResultType parseFromValue(boolean value)
  {
    for (ResultType type : values()) {
      if (type.getValue() == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型：" + value);
  }

  private ResultType(String label, boolean value) {
    this.label = label;
    this.value = value;
  }

  public boolean getValue() {
    return this.value;
  }
  
  public String getLabel() {
    return this.label;
  }
  
  public String toString() {
    return this.label + "[" + this.value + "]";
  }
  
}
