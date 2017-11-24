package com.broada.carrier.monitor.impl.storage.huawei.oceanstor.raid;

import java.io.Serializable;


/**
 * 存储池健康状态
 * 
 * @author yanwl 
 * Create By 2017-08-07 下午16:00:29
 */
public enum HealthStatus implements Serializable{
	
	UNKNOW(0, "未知"), 
	
	NORMAL(1, "正常"), 

	FAULT(2, "故障"), 
	
	PRE_FAIL(3, "即将故障"),
	
	PARTIALLY_BROKEN(4, "部分损坏"), 
	
	DEGRADED(5, "降级"), 
	
	BAD_SECTORS_FOUND(6, "有坏块"), 
	
	BIT_ERRORS_FOUND(7, "有误码"), 
	
	CONSISTENT(8, "一致"),
	
	IN_CONSISTENT(9, "不一致"),
	
	BUSY(10, "繁忙"),
	
	NO_INPUT(11, "无输入"),
	
	LOW_BATTERY(12, "电量不足"),
	
	SINGLE_LINK_FAULT(13, "单链路故障"),
	
	INVALID(14, "失效"),
	
	WRITE_PROTECT(15, "写保护");
	
  private int label;
  private String value;

  public static HealthStatus parseFromValue(String value)
  {
    for (HealthStatus type : values()) {
      if (type.getValue() == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型value=：" + value);
  }
  
  public static HealthStatus parseFromLable(int label)
  {
    for (HealthStatus type : values()) {
      if (type.getLabel() == label) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型label=：" + label);
  }

  private HealthStatus(int label, String value) {
    this.label = label;
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
  
  public int getLabel() {
    return this.label;
  }
  
  public String toString() {
    return this.label + "[" + this.value + "]";
  }
}
