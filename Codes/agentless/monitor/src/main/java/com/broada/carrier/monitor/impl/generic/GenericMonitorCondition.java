package com.broada.carrier.monitor.impl.generic;

import com.broada.carrier.monitor.method.common.MonitorCondition;
/**
 * 本实体父类变量没多大意义,只有field需要重写，MonitorConditionHolder实体存储具体比较条件
 * 如果存在实例，则存储对应该实例的比较条件，否则，存储无实例时的比较条件
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2010-4-8 上午10:30:24
 */
public class GenericMonitorCondition extends MonitorCondition {
  private static final long serialVersionUID = -3231251745513068512L;
  private MonitorConditionHolder[] holders;//真实的比较条件
  private boolean hasInstance;
  private String instanceName;
  public GenericMonitorCondition() {
  }

  /**
   * 构造方法
   * 
   * @param holders
   */
  public GenericMonitorCondition(MonitorConditionHolder[] holders) {
    this.holders = holders;
    setField(getIntanceName());
  }
  
  /**
   * 获取实例名
   * 
   * @return
   */
  public String getIntanceName() {
    if (instanceName == null) {
      if (holders != null && holders.length > 0) {
        instanceName = holders[0].getInstance();
      }
    }
    return instanceName;
  }
  
  /**
   * 设置实例名
   * 
   * @param instanceName
   */
  public void setIntanceName(String instanceName){
    this.instanceName = instanceName;
    this.hasInstance = true;
    setField(instanceName);
  }
  
  public MonitorConditionHolder[] getHolders() {
    return holders;
  }

  public boolean isHasInstance() {
    if (!hasInstance) {
      if (holders != null && holders.length > 0) {
        hasInstance = holders[0].isHasInstance();
      }
    }
    return hasInstance;
  }
}
