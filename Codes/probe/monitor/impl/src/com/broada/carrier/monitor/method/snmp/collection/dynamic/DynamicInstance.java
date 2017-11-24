package com.broada.carrier.monitor.method.snmp.collection.dynamic;

import com.broada.carrier.monitor.method.snmp.collection.entity.PerfType;

/**
 * 动态性能资源实例
 * @author Maico Pang (panghf@broada.com.cn)
 * Create By 2007-5-23 10:56:56
 */
public interface DynamicInstance {
  /**
   * 获取属性的元数据信息列表
   * @return
   */
  public PropertyDefine[] getPropertyDefines();
  /**
   * 根据属性名获取属性
   * @param propertyName
   * @return
   */
  public Property getProperty(String propertyName);
  /**
   * 获取该实例的性能类型
   * @return
   */
  public PerfType getPerfType();
}
