package com.broada.carrier.monitor.spi;

import java.awt.Component;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;

/**
 * 监测器配置界面
 * @author Jiangjw
 */
public interface MonitorConfiger {
	/**
	 * 返回配置界面组件
	 * @return
	 */	
	Component getComponent();

	/**
	 * 从界面读取数据，将数据写到{@link #setData(MonitorConfigContext)}设置的data中
	 * @return 如果界面参数正确，返回true
	 */	
	boolean getData();

  /**
   * 将数据写到界面
   * @param context
   */
	void setData(MonitorConfigContext data);

	/**
	 * 当用户在界面上选择了监测方法时触发，会将用户选择的监测方法通过参数传递进来
	 * @param method
	 */
	void setMethod(MonitorMethod method);
}
