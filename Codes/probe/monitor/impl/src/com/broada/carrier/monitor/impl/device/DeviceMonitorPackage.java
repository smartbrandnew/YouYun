package com.broada.carrier.monitor.impl.device;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.device.cpu.CPUMonitor;
import com.broada.carrier.monitor.impl.device.ifperf.IntfPerfMonitor;
import com.broada.carrier.monitor.impl.device.ifstatus.IfstatusMonitor;
import com.broada.carrier.monitor.impl.device.ram.RAMMonitor;
import com.broada.carrier.monitor.impl.device.temperature.TemperatureMonitor;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class DeviceMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "NetDev", "SecDev" };
		String[] targetTypeWithOSIds = new String[] { "NetDev", "SecDev", "OS" };
		String[] methodTypeIds = new String[] { SnmpMethod.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("NETDEVICE","CPU", "设备CPU利用率", "使用Snmp协议监测指定的网络设备的CPU使用百分比(利用率)。",
						MultiInstanceConfiger.class.getName(),
						CPUMonitor.class.getName(),
						index++, targetTypeIds, methodTypeIds),

				new MonitorType("NETDEVICE","RAM", "设备内存利用率", "使用Snmp协议监测指定的网络设备的内存使用百分比利用率(利用率)。",
						MultiInstanceConfiger.class.getName(),
						RAMMonitor.class.getName(),
						index++, targetTypeIds, methodTypeIds),

				new MonitorType("NETDEVICE","TEMPERATURE", "网络设备温度", "监测网络设备的温度情况",
						MultiInstanceConfiger.class.getName(),
						TemperatureMonitor.class.getName(),
						index++, targetTypeIds, methodTypeIds),

				new MonitorType("NETDEVICE","IFSTATUS", "端口上下线", "监测设备的端口上下线情况",
						MultiInstanceConfiger.class.getName(),
						IfstatusMonitor.class.getName(),
						index++, targetTypeIds, methodTypeIds),
						
				new MonitorType("NETDEVICE","INTFPREF", "端口速率监测", "使用Snmp协议监测网络设备端口的出入速率。",
						MultiInstanceConfiger.class.getName(),
						IntfPerfMonitor.class.getName(),
						index++, targetTypeWithOSIds, methodTypeIds),
		};
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("CPU","CPU-1", "CPU使用率", "%", "指定网络设备的CPU使用率", MonitorItemType.NUMBER),

				new MonitorItem("RAM", "RAM-1", "内存使用率", "%", "指定网络设备的内存使用率", MonitorItemType.NUMBER),
				
				new MonitorItem("TEMPERATURE", "TEMPERATURE-1", "网络设备温度", "℃", "指定网络设备的温度情况", MonitorItemType.NUMBER),
				
				new MonitorItem("IFSTATUS", "IFSTATUS-1", "端口状态", "", "状态操作状态", MonitorItemType.TEXT),
				
				new MonitorItem("IFSTATUS","IFSPEED-1", "端口带宽", "Mbps", "带宽", MonitorItemType.NUMBER),
				
				new MonitorItem("INTFPREF", "INTFPREF-1", "端口入速率", "kbps", "网络设备端口的入速率", MonitorItemType.NUMBER),
				new MonitorItem("INTFPREF", "INTFPREF-2", "端口出速率", "kbps", "网络设备端口的出速率", MonitorItemType.NUMBER),
				new MonitorItem("INTFPREF", "INTFPREF-3", "端口入丢帧速", "pps", "端口入丢帧速", MonitorItemType.NUMBER),
				new MonitorItem("INTFPREF", "INTFPREF-4", "端口出丢帧速", "pps", "端口出丢帧速", MonitorItemType.NUMBER),
				new MonitorItem("INTFPREF", "INTFPREF-5", "单播入帧速", "pps", "通过的单播入帧速", MonitorItemType.NUMBER),
				new MonitorItem("INTFPREF", "INTFPREF-6", "单播出帧速", "pps", "通过的单播出帧速", MonitorItemType.NUMBER),
				new MonitorItem("INTFPREF", "INTFPREF-7", "非单播入帧速", "pps", "通过的非单播入帧速", MonitorItemType.NUMBER),
				new MonitorItem("INTFPREF", "INTFPREF-8", "非单播出帧速", "pps", "通过的非单播出帧速", MonitorItemType.NUMBER),
				new MonitorItem("INTFPREF", "INTFPREF-9", "入错误帧速", "pps", "通过端口的入数据错误帧速", MonitorItemType.NUMBER),
				new MonitorItem("INTFPREF", "INTFPREF-10", "出错误帧速", "pps", "通过端口的出数据错误帧速", MonitorItemType.NUMBER),
		};
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[0];
	}
}
