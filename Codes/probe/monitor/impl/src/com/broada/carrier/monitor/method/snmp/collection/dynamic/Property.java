package com.broada.carrier.monitor.method.snmp.collection.dynamic;

/**
 * 属性对象
 * 
 * @author Maico Pang (panghf@broada.com.cn)
 * Create By 2007-5-23 15:25:37
 */
public class Property {
	private PropertyDefine define;
	private String value = "";
	
	public Property(PropertyDefine define) {	
    this.define=define;
	}
  public Property(PropertyDefine define,String value) {  
    this.define=define;
    this.value=value;
  }

  public String getName() {
		return define.getName();
	}
  
  public DataType getType(){
    return define.getType();
  }
  
  public PropertyDefine getDefine(){
    return define;
  }

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
  public void setValue(int value) {
    this.value = Integer.toString(value);
  } 
  public void setValue(double value) {
    this.value = Double.toString(value);
  }
  public void setValue(boolean value) {
    this.value = Boolean.toString(value);
  }
  public void setValue(long value) {
    this.value = Long.toString(value);
  }
	
	public int getIntegerValue() {
		return Integer.parseInt(value.toString());
	}
	
  public long getLongValue() {
    return Long.parseLong(value.toString());
  }  
  
	public double getDoubleValue() {
  	return Double.parseDouble(value.toString());
	}
	
	public boolean getBooleanValue() {
		return Boolean.getBoolean(value.toString());
	}
	public String toString() {
		return "Property[" +getType()+"-"+ getName() +" - " + getValue() + "]";
	}
}
