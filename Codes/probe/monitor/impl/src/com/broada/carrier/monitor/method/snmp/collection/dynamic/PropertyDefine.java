package com.broada.carrier.monitor.method.snmp.collection.dynamic;

import java.io.Serializable;

/**
 * 属性定义对象
 * 
 * @author Maico Pang (panghf@broada.com.cn)
 * Create By 2007-5-23 15:27:04
 */
public class PropertyDefine implements Serializable {
	/**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -1918749659085298169L;

	
  private String name;
  private int index=0;//索引号
	private DataType type;
	private String descr;
	
	public PropertyDefine(String name, DataType type) {
    this.name=name;
    this.type=type;
	}
	
	public PropertyDefine(String name, DataType type, String descr) {
		this.name = name;
		this.type = type;
		this.descr = descr;
	}	

	public String getDescr() {
		return descr;
	}
	public String getName() {
		return name;
	}
	public DataType getType() {
		return type;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(DataType type) {
		this.type = type;
	}

	public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public String toString() {
		return "PropertyDefine[" + name + " - " + type + " - " + descr + "]";
	}	
}
