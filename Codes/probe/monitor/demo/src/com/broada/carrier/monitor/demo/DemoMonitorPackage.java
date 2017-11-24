package com.broada.carrier.monitor.demo;

import com.broada.carrier.monitor.demo.jvm.JVMMonitor2;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

/**
 * 监测器扩展包的导出类
 * 用于注册监测器，实现此类后，需要将此类的路径复制到当前jar的“META-INF\services\com.broada.carrier.monitor.spi.MonitorPackage”文件中
 * 
 * @author Jiangjw
 */
public class DemoMonitorPackage implements MonitorPackage {

	/**
	 * 提供所有监测器注册信息
	 */
	@Override
	public MonitorType[] getTypes() {
		return new MonitorType[]{
				JVMMonitor2.TYPE
		};
	}

	/**
	 * 提供所有监测指标注册信息
	 */
	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				JVMMonitor2.MI_HEAP_USED,
				JVMMonitor2.MI_HOME,
				JVMMonitor2.MI_OS
		};
	}

	/**
	 * 提供所有监测协议注册信息
	 */
	@Override
	public MonitorMethodType[] getMethodTypes() {
		return null;
	}

}
