package com.broada.carrier.monitor.server.api.entity;

import java.io.Serializable;

public interface MonitorParameter extends Serializable {
  /**
   * 把原来保存的字符串格式的监测参数付给监测参数实例
   * 实例主要完成字符串到监测参数实例之间的转换工作
   *
   *
   * @param xml 监测参数字符串(一般是XML格式)
   */
  void setParameters(String xml);

  /**
   * 返回配置后的监测参数字符串(一般是XML格式)
   * @return
   */
  String getParameters();

  /**
   * 返回当前配置的监测点的简单描述(主要用于框架生成简单的配置描述信息)
   * @return
   */
  String getSimpleDesc();
  
  /**
   * 返回当前监测服务的特征
   * <p>
   * 监测框架会组合节点的IP、监测器标识和该特征作为一个监测服务的唯一关键字
   * @return
   */
  String getStamp();
}