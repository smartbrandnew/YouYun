package com.broada.carrier.monitor.impl.mw.tomcat;

public class PerfItemMap {
  /**
   * 这个是MonitorItem的code
   */
  private String code;
  
  /**
   * 这个name不同于MonitorItem的name，这个是自定义的
   */
  private String name;
  
  /**
   * 实际监测出来的值
   */
  private Object value;

  public PerfItemMap(){
    
  }
  
  public PerfItemMap(String _code,String _name,Object _value){
	code=_code;
    name=_name;
    value=_value;
  }  

  public String getCode() {
	return code;
  }

  public void setCode(String code) {
	this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }
}
