package com.broada.carrier.monitor.impl.host.ipmi.sdk.api;

import java.io.Serializable;


/**
 * SDR信息结果类型
 * 
 * @author pippo 
 * Create By 2013-7-30 下午7:00:29
 */
public enum SDRType implements Serializable{
	
	OK("ok", "1"), 
	
	NS("ns", "2"), 
	
	LNS("lns", "1"), 

	NC("nc", "1"), 
	
	LNC("lnc", "1"), 
	
	CR("cr", "0"), 
	
	LCR("lcr", "0"), 
	
	LNR("lnr", "0"), 
	
	NR("nr", "0");
	
  private String label;
  private String value;

  public static SDRType parseFromValue(String value)
  {
    for (SDRType type : values()) {
      if (type.getValue().equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型value=：" + value);
  }
  
  public static SDRType parseFromLable(String label)
  {
    for (SDRType type : values()) {
      if (type.getLabel().equalsIgnoreCase(label)) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型label=：" + label);
  }

  private SDRType(String label, String value) {
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
