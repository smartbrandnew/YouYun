package com.broada.carrier.monitor.impl.storage.huawei.oceanstor.info;

import java.io.Serializable;


/**
 * 主机操作系统
 * 
 * @author yanwl 
 * Create By 2017-08-08 11:00:29
 */
public enum OperatingSystem implements Serializable{
	LINUX(0, "Linux"), 
	
	WINDOWS(1, "Windows"), 

	SOLARIS(2, "Solaris"), 
	
	HP_UX(3, "HP-UX"),
	
	AIX(4, "AIX"), 
	
	XEN_SERVER(5, "XenServer"), 
	
	MAC_OC(6, "Mac OS"), 
	
	VMWARE_ESX(7, "VMware ESX"), 
	
	VIS_6000(8, "VIS6000"),
	
	WINDOWS_SERVER_2012(9, "Windows Server 2012");
	
  private int label;
  private String value;

  public static OperatingSystem parseFromValue(String value)
  {
    for (OperatingSystem type : values()) {
      if (type.getValue() == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型value=：" + value);
  }
  
  public static OperatingSystem parseFromLable(int label)
  {
    for (OperatingSystem type : values()) {
      if (type.getLabel() == label) {
        return type;
      }
    }
    throw new IllegalArgumentException("未定义的状态类型label=：" + label);
  }

  private OperatingSystem(int label, String value) {
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
