package com.broada.carrier.monitor.impl.host.ipmi.sdk.api;

import java.io.Serializable;


/**
 * 服务器类型
 * 
 * @author pippo 
 * Create By 2013-7-30 下午7:00:29
 */
public enum ServerType implements Serializable{
	
	DELL("DELL", "Board Mfg"), 
	
	IBM("IBM", "Board Mfg"), 
	
	ERROR("ERROR", "BMC地址或用户名密码错误！"), 
	
	UNK("UNKNOWN", "无法获得服务器厂商！"), 
	
  HP("HP", "Product Manufacturer");
	
  private String value;
  private String label;

  public static ServerType parseFromValue(String value)
  {
    for (ServerType type : values()) {
      if (type.getValue() == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型：" + value);
  }

  private ServerType(String label, String value) {
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
