package com.broada.carrier.monitor.impl.common.convertor;

/**
 * 监测结果单位转换器
 * 
 * @author lixy Sep 22, 2008 11:46:28 AM
 */
public interface MonResultConvertor {

  /**
   * 执行单位转换操作
   * 
   * @param srcValue
   *          转换单位前的数据
   * @return 转换单位后的数据
   */
  public double doConvert(double srcValue);
}
