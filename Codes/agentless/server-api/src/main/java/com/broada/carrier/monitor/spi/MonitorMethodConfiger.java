package com.broada.carrier.monitor.spi;

import java.awt.Component;

import com.broada.carrier.monitor.spi.entity.MonitorMethodConfigContext;

/**
 * 监测方法配置界面接口
 * @author Jiangjw
 */
public interface MonitorMethodConfiger {
	/**
	 * 返回配置界面组件
	 * @return
	 */
	Component getComponent();
  
	/**
	 * 从界面读取数据，将数据写到{@link #setData(MonitorMethodConfigContext)}设置的context中
	 * @return 如果界面参数正确，返回true
	 */
  boolean getData();
  
  /**
   * 将数据写到界面
   * @param context
   */
  void setData(MonitorMethodConfigContext context);
}
