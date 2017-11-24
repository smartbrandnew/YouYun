package com.broada.carrier.monitor.impl.host.ipmi.chassis;

import java.io.Serializable;


/**
 * 底盘信息类型
 * 
 * @author pippo 
 * Create By 2013-7-30 下午7:00:29
 */
public enum ChassisType implements Serializable{
	
	SYSTEMPOWER("电源启用状态", "IPMI-CHASSIS-1"), 
	
	POWERINTERLOCK("电源连锁", "IPMI-CHASSIS-3"), 

	MAINPOWERFAULT("主电源故障", "IPMI-CHASSIS-4"), 
	
	POWERCONTROLFAULT("功率控制故障", "IPMI-CHASSIS-5"), 
	
	CHASSISINTRUSION("机箱启用", "IPMI-CHASSIS-6"), 
	
	PANELLOCKOUT("面板锁定", "IPMI-CHASSIS-7"), 
	
	DRIVERFAULT("驱动故障", "IPMI-CHASSIS-8"), 
	
	RADIATINGFAULT("散热故障", "IPMI-CHASSIS-9"), 
	
	POWEROVERLOAD("功率过载", "IPMI-CHASSIS-2");
	
  private String label;
  private String value;

  public static ChassisType parseFromValue(String value)
  {
    for (ChassisType type : values()) {
      if (type.getValue() == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型value=：" + value);
  }
  
  public static ChassisType parseFromLable(String label)
  {
    for (ChassisType type : values()) {
      if (type.getLabel().equalsIgnoreCase(label)) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型label=：" + label);
  }

  private ChassisType(String label, String value) {
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
