package com.broada.carrier.monitor.method.snmp.collection.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class PerfType {	
	public static final PerfType CPU = new PerfType("cpu","CPU");
	public static final PerfType MEM = new PerfType("mem", "内存");
	public static final PerfType TEMP = new PerfType("temp", "温度");
  public static final PerfType DISK = new PerfType("disk", "磁盘");
	
	private static List<PerfType> instances = new ArrayList<PerfType>();
	
  static {
    instances.add(CPU);
    instances.add(MEM);
    instances.add(TEMP);
    instances.add(DISK);
  }
  
	private String id;
	private String name;
	
	private PerfType(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}
	
  public String toString() {
    return name;
  }

  /**
   * 根据ID获取性能类型对象
   * @param id
   * @return
   * @exception IllegalArgumentException 如果不存在指定ID的类型则抛出
   */
  public static PerfType getInstance(String id){
		Iterator<PerfType> iter = instances.iterator();
		while (iter.hasNext()) {
			PerfType instance = (PerfType)iter.next();
			if (instance.getId().compareToIgnoreCase(id) == 0)
				return instance;
		}
    
		throw new IllegalArgumentException("无法识别的枚举[" + PerfType.class.getName() + "]ID：" + id);
	}	
}
