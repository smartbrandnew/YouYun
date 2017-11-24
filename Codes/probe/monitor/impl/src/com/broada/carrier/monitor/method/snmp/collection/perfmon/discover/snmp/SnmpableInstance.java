package com.broada.carrier.monitor.method.snmp.collection.perfmon.discover.snmp;

import java.util.LinkedHashMap;
import java.util.Map;

import com.broada.carrier.monitor.method.snmp.collection.dynamic.DataType;
import com.broada.carrier.monitor.method.snmp.collection.dynamic.DynamicInstance;
import com.broada.carrier.monitor.method.snmp.collection.dynamic.Property;
import com.broada.carrier.monitor.method.snmp.collection.dynamic.PropertyDefine;
import com.broada.carrier.monitor.method.snmp.collection.entity.PerfType;

/**
 * 支持Snmp协议的实例对象
 * @author Maico Pang (panghf@broada.com.cn)
 * Create By 2007-5-23 15:32:12
 */
public class SnmpableInstance implements DynamicInstance {
  private String key;
  private String name;
  private String index;
  private PerfType perfType;
  private String utilizeExp;
  
  private static final Map<String, PropertyDefine> defines=new LinkedHashMap<String, PropertyDefine>();
  static{
    defines.put("key",new PropertyDefine("key",DataType.TYPE_STRING,"key"));
    defines.put("name",new PropertyDefine("name",DataType.TYPE_STRING,"Name"));
    defines.put("index",new PropertyDefine("index",DataType.TYPE_STRING,"Index"));
    defines.put("type",new PropertyDefine("type",DataType.TYPE_STRING,"类型"));
    defines.put("exp",new PropertyDefine("exp",DataType.TYPE_STRING,"表达式"));
  }
  
  /**
   * 
   */
  public SnmpableInstance(String key,String name,PerfType type) {
    super();
    this.key=key;
    this.name=name;
    this.perfType=type;
  }

  /*
   * @see com.broada.collection.dynamic.DynamicInstance#getPropertyDefines()
   */
  public PropertyDefine[] getPropertyDefines() {
    return (PropertyDefine[])defines.values().toArray(new PropertyDefine[0]);
  }

  /*
   * @see com.broada.collection.dynamic.DynamicInstance#getProperty(java.lang.String)
   */
  public Property getProperty(String propertyName) {
    if("key".equals(propertyName)){
      return new Property((PropertyDefine)defines.get("key"),getKey());
    }else if("name".equals(propertyName)){
      return new Property((PropertyDefine)defines.get("name"),getName());
    }else if("index".equals(propertyName)){
      return new Property((PropertyDefine)defines.get("index"),getIndex());
    }else if("type".equals(propertyName)){
      return new Property((PropertyDefine)defines.get("type"),getPerfType().getId());
    }else if("exp".equals(propertyName)){
      return new Property((PropertyDefine)defines.get("exp"),getUtilizeExp());
    }else{
      return null;
    }
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPerfType(PerfType type) {
    this.perfType = type;
  }

  public String getUtilizeExp() {
    return utilizeExp;
  }

  public void setUtilizeExp(String utilizeExp) {
    this.utilizeExp = utilizeExp;
  }

  public PerfType getPerfType() {
    return perfType;
  }

  public String getIndex() {
    return index;
  }

  public void setIndex(String index) {
    this.index = index;
  }
}
