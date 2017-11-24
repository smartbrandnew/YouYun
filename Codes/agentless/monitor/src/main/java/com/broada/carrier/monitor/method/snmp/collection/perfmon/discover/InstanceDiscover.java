package com.broada.carrier.monitor.method.snmp.collection.perfmon.discover;

import com.broada.carrier.monitor.method.snmp.collection.dynamic.DynamicInstance;
import com.broada.carrier.monitor.method.snmp.collection.entity.PerfType;
import com.broada.snmputil.SnmpTarget;

public interface InstanceDiscover {
  /**
   * 发现指定IP的性能资源实例
   * @param node
   * @param timeout 超时时间(毫秒)
   * @param type 需要获取的性能类型
   * @return 如果获取不到则返回null
   * @exception InstanceDiscoverException 当进行发现的过程中发现异常可以抛出
   */
  public DynamicInstance[] discover(SnmpTarget node,int timeout,PerfType type) throws InstanceDiscoverException;
}
