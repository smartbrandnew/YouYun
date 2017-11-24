package com.broada.carrier.monitor.impl.host.ipmi;

import com.broada.carrier.monitor.impl.common.SpecificMultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SpecificSingleInstanceConfig;
import com.broada.carrier.monitor.impl.host.ipmi.basic.IPMIBasicMonitor;
import com.broada.carrier.monitor.impl.host.ipmi.chassis.IPMIChassisMonitor;
import com.broada.carrier.monitor.impl.host.ipmi.health.IPMIHealthMonitor;
import com.broada.carrier.monitor.impl.host.ipmi.perfs.IPMIPerfsMonitor;
import com.broada.carrier.monitor.method.ipmi.IPMIConfigPanel;
import com.broada.carrier.monitor.method.ipmi.IPMIMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

/**
 * Created by hu on 2015/5/18.
 */
public class IPMIMonitorPackage implements MonitorPackage {
	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "Server" };
		String[] methodTypeIds = new String[] { IPMIMonitorMethodOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("IPMI", "IPMI-BASIC", "主机基本信息监测(IPMI)", "使用IPMI协议监测服务器的基本信息。",
						SpecificMultiInstanceConfiger.class.getName(), IPMIBasicMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				new MonitorType("IPMI", "IPMI-CHASSIS", "主机底盘信息监测(IPMI)", "使用IPMI协议监测服务器的底盘信息。",
						SpecificSingleInstanceConfig.class.getName(), IPMIChassisMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				new MonitorType("IPMI", "IPMI-HEALTH", "主机健康信息监测(IPMI)", "使用IPMI协议监测服务器的健康信息。",
						SpecificMultiInstanceConfiger.class.getName(), IPMIHealthMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				new MonitorType("IPMI", "IPMI-QUOTA", "主机指标信息监测(IPMI)", "使用IPMI协议监测服务器的指标信息判断是否正常。",
						SpecificMultiInstanceConfiger.class.getName(), IPMIPerfsMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds)};
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] { new MonitorItem("IPMI-BASIC", "IPMI-BASIC-1", "厂商", "", "厂商名称", MonitorItemType.TEXT),
				new MonitorItem("IPMI-BASIC", "IPMI-BASIC-2", "产品", "", "产品名称", MonitorItemType.TEXT),
				new MonitorItem("IPMI-BASIC", "IPMI-BASIC-3", "序列号", "", "产品序列号", MonitorItemType.TEXT),
				new MonitorItem("IPMI-BASIC", "IPMI-BASIC-4", "型号", "", "产品型号", MonitorItemType.TEXT),
				new MonitorItem("IPMI-BASIC", "IPMI-BASIC-5", "输入电压", "", "电源输入电压", MonitorItemType.TEXT),
				new MonitorItem("IPMI-BASIC", "IPMI-BASIC-6", "输入频率", "", "电源输入频率", MonitorItemType.TEXT),
				new MonitorItem("IPMI-BASIC", "IPMI-BASIC-7", "额定功率", "", "电源额定功率", MonitorItemType.TEXT),
				new MonitorItem("IPMI-BASIC", "IPMI-BASIC-8", "特性", "", "电源特性", MonitorItemType.TEXT),
				new MonitorItem("IPMI-BASIC", "IPMI-BASIC-9", "IP", "", "IP地址", MonitorItemType.TEXT),
				new MonitorItem("IPMI-CHASSIS", "IPMI-CHASSIS-1", "电源启用状态", "", "电源启用状态（是/否）", MonitorItemType.TEXT),
				new MonitorItem("IPMI-CHASSIS", "IPMI-CHASSIS-2", "功率过载", "", "功率过载（是/否）", MonitorItemType.TEXT),
				new MonitorItem("IPMI-CHASSIS", "IPMI-CHASSIS-3", "电源连锁", "", "电源连锁（是/否）", MonitorItemType.TEXT),
				new MonitorItem("IPMI-CHASSIS", "IPMI-CHASSIS-4", "主电源故障", "", "主电源故障（是/否）", MonitorItemType.TEXT),
				new MonitorItem("IPMI-CHASSIS", "IPMI-CHASSIS-5", "功率控制故障", "", "功率控制故障（是/否）", MonitorItemType.TEXT),
				new MonitorItem("IPMI-CHASSIS", "IPMI-CHASSIS-6", "机箱启用", "", "机箱启用（是/否）", MonitorItemType.TEXT),
				new MonitorItem("IPMI-CHASSIS", "IPMI-CHASSIS-7", "面板锁定", "", "面板锁定（是/否）", MonitorItemType.TEXT),
				new MonitorItem("IPMI-CHASSIS", "IPMI-CHASSIS-8", "驱动故障", "", "驱动故障（是/否）", MonitorItemType.TEXT),
				new MonitorItem("IPMI-CHASSIS", "IPMI-CHASSIS-9", "散热故障", "", "散热故障（是/否）", MonitorItemType.TEXT),
				new MonitorItem("IPMI-HEALTH", "IPMI-HEALTH-1", "状态", "", "运行状态", MonitorItemType.TEXT),
				new MonitorItem("IPMI-QUOTA", "IPMI-QUOTA", "指标值", "", "IPMI指标", MonitorItemType.TEXT),
				new MonitorItem("IPMI-QUOTA", "IPMI-QUOTA-1", "温度", "℃", "服务器元件的温度", MonitorItemType.NUMBER),
				new MonitorItem("IPMI-QUOTA", "IPMI-QUOTA-2", "电压", "V", "服务器元件的电压", MonitorItemType.NUMBER),
				new MonitorItem("IPMI-QUOTA", "IPMI-QUOTA-3", "电流", "A", "服务器电源的电流", MonitorItemType.NUMBER),
				new MonitorItem("IPMI-QUOTA", "IPMI-QUOTA-4", "功率", "W", "服务器主板的功率", MonitorItemType.NUMBER),
				new MonitorItem("IPMI-QUOTA", "IPMI-QUOTA-5", "转速", "RPM", "服务器风扇的转速", MonitorItemType.NUMBER) };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(IPMIMonitorMethodOption.TYPE_ID, "IPMI监测协议",
				IPMIConfigPanel.class) };
	}
}
