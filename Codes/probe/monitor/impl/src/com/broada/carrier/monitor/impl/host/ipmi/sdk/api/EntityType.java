package com.broada.carrier.monitor.impl.host.ipmi.sdk.api;

import java.io.Serializable;



/**
 * IPMI实体
 * 
 * @author pippo 
 * Create By 2013-7-30 下午7:00:29
 */
public enum EntityType implements Serializable{
	
  PROCE("CPU", 3), 
	
  BOARD("主板", 7), 

  MEMORY("内存", 8),
  
  FAN("风扇", -1),
  
  POWER("电源", 10);
	
  private String label;
  private int value;

  public static EntityType parseFromValue(int value)
  {
    for (EntityType type : values()) {
      if (type.getValue() == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型：value=" + value);
  }
  
  public static EntityType parseFromLabel(String label)
  {
    for (EntityType type : values()) {
      if (type.getLabel().equalsIgnoreCase(label)) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的实体类型：label=" + label);
  }

  private EntityType(String label, int value) {
    this.value = value;
    this.label = label;
  }

  public int getValue() {
    return this.value;
  }
  
  public void setValue(int value) {
    this.value = value;
  }

  public String getLabel() {
    return this.label;
  }

  public String toString() {
    return this.label + "[" + this.value + "]";
  }
  
}
