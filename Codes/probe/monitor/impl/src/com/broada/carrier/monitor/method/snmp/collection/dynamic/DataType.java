package com.broada.carrier.monitor.method.snmp.collection.dynamic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 属性数据类型
 * @author Maico Pang (panghf@broada.com.cn)
 * Create By 2007-5-23 11:38:21
 */
public class DataType implements Serializable {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -5021871888253707656L;

  public static final DataType TYPE_INTEGER = new DataType("integer","整型");
  public static final DataType TYPE_STRING = new DataType("string","字符串"); 
  public static final DataType TYPE_DOUBLE = new DataType("double","双精度");
  public static final DataType TYPE_LONG = new DataType("long","长整型");
  public static final DataType TYPE_OBJECT = new DataType("object","对象");
  private static List<DataType> instances = new ArrayList<DataType>();

  static {
    instances.add(TYPE_INTEGER);
    instances.add(TYPE_STRING);
    instances.add(TYPE_DOUBLE);
    instances.add(TYPE_LONG);
  }

  private String id;
  private String name;

  private DataType(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getId() {
    return id;
  }

  /**
   * 根据ID获取属性类型对象
   * @param id
   * @return
   * @exception IllegalArgumentException 如果不存在指定ID的类型则抛出
   */
  public static DataType getInstance(String id) {
    Iterator<DataType> iter = instances.iterator();
    while (iter.hasNext()) {
      DataType instance = (DataType) iter.next();
      if (instance.getId().equalsIgnoreCase(id))
        return instance;
    }
    throw new IllegalArgumentException("无法识别的枚举[" + DataType.class.getName() + "]ID：" + id);
  }
  
  public String toString(){
    return id;
  }

}
