package com.broada.carrier.monitor.impl.storage.dell.equallogic;

import com.broada.carrier.monitor.impl.storage.StorageConfiger;
import com.broada.carrier.monitor.impl.storage.dell.equallogic.controller.ControllerInfoMonitor;
import com.broada.carrier.monitor.impl.storage.dell.equallogic.disk.DiskInfoMonitor;
import com.broada.carrier.monitor.impl.storage.dell.equallogic.fan.FanInfoMonitor;
import com.broada.carrier.monitor.impl.storage.dell.equallogic.group.GroupInfoMonitor;
import com.broada.carrier.monitor.impl.storage.dell.equallogic.ipconf.IPConfInfoMonitor;
import com.broada.carrier.monitor.impl.storage.dell.equallogic.member.MemberInfoMonitor;
import com.broada.carrier.monitor.impl.storage.dell.equallogic.powersupply.PowerSupplyInfoMonitor;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class DELLEqualLogicMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "DELL" };
		String[] methodTypeIds = new String[] { SnmpMethod.TYPE_ID };
		int index = 1;

		String multiInstanceClz = StorageConfiger.class.getName();

		return new MonitorType[] {
				new MonitorType("DELL_EQUALLOGIC", "DELLEQUALLOGIC-DISKDRIVER-INFO", "DELLEQUALLOGIC物理磁盘基本信息监测",
						"监测阵列物理磁盘基本信息及容量使用情况。", multiInstanceClz, DiskInfoMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("DELL_EQUALLOGIC", "DELLEQUALLOGIC-FAN-INFO", "DELLEQUALLOGIC风扇基本信息监测",
						"监测阵列存储卷基本信息及容量使用情况。", multiInstanceClz, FanInfoMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("DELL_EQUALLOGIC", "DELLEQUALLOGIC-IPCONF-INFO", "DELLEQUALLOGICip信息基本信息监测",
						"监测阵列存储池基本信息及容量使用情况。", multiInstanceClz, IPConfInfoMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("DELL_EQUALLOGIC", "DELLEQUALLOGIC-MEMBER-INFO", "DELLEQUALLOGIC部件基本信息监测",
						"监测阵列不见基本信息及容量使用情况。", multiInstanceClz, MemberInfoMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("DELL_EQUALLOGIC", "DELLEQUALLOGIC-POWERSUPPLY-INFO", "DELLEQUALLOGIC电源基本信息监测",
						"监测控制器上光纤端口基本信息及容量使用情况。", multiInstanceClz, PowerSupplyInfoMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("DELL_EQUALLOGIC", "DELLEQUALLOGIC-GROUP-INFO", "DELLEQUALLOGIC分组基本信息监测",
						"监测阵列逻辑磁盘基本信息及容量使用情况。", multiInstanceClz, GroupInfoMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("DELL_EQUALLOGIC", "DELLEQUALLOGIC-CONTORLLER-INFO", "DELLEQUALLOGIC控制器基本信息监测",
						"监测控制器基本信息及容量使用情况。", multiInstanceClz, ControllerInfoMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType(
						"DELL_EQUALLOGIC",
						"DELLEQUALLOGIC-CONTORLLER-CHIPTEMP-INFO",
						"DELLEQUALLOGIC控制器芯片温度信息监测",
						"监测控制器网络端口基本信息及容量使用情况。",
						multiInstanceClz,
						com.broada.carrier.monitor.impl.storage.dell.equallogic.controller.chipTemp.ControllerInfoMonitor.class
								.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType(
						"DELL_EQUALLOGIC",
						"DELLEQUALLOGIC-CONTORLLER-PROCTEMP-INFO",
						"DELLEQUALLOGIC控制器处理器温度信息监测",
						"监测控制器网络端口基本信息及容量使用情况。",
						multiInstanceClz,
						com.broada.carrier.monitor.impl.storage.dell.equallogic.controller.procTemp.ControllerInfoMonitor.class
								.getName(), index++, targetTypeIds, methodTypeIds) };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("DELLEQUALLOGIC-DISKDRIVER-INFO", "DELLEQUALLOGIC-DISKDRIVER-INFO-1", "磁盘编号", "",
						"磁盘编号", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-DISKDRIVER-INFO", "DELLEQUALLOGIC-DISKDRIVER-INFO-5", "磁盘状态", "",
						"磁盘状态", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-DISKDRIVER-INFO", "DELLEQUALLOGIC-DISKDRIVER-INFO-8", "所属成员设备名称", "",
						"所属成员设备名称", MonitorItemType.TEXT),

				new MonitorItem("DELLEQUALLOGIC-FAN-INFO", "DELLEQUALLOGIC-FAN-INFO-1", "风扇名称", "", "风扇名称",
						MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-FAN-INFO", "DELLEQUALLOGIC-FAN-INFO-2", "风扇状态", "", "风扇状态",
						MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-FAN-INFO", "DELLEQUALLOGIC-FAN-INFO-3", "所属成员设备名称", "", "所属成员设备名称",
						MonitorItemType.TEXT),

				new MonitorItem("DELLEQUALLOGIC-IPCONF-INFO", "DELLEQUALLOGIC-IPCONF-INFO-1", "网络接口编号", "", "网络接口编号",
						MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-IPCONF-INFO", "DELLEQUALLOGIC-IPCONF-INFO-2", "网络接口名称", "", "网络接口名称",
						MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-IPCONF-INFO", "DELLEQUALLOGIC-IPCONF-INFO-3", "IP地址", "", "IP地址",
						MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-IPCONF-INFO", "DELLEQUALLOGIC-IPCONF-INFO-4", "子网掩码", "", "子网掩码",
						MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-IPCONF-INFO", "DELLEQUALLOGIC-IPCONF-INFO-5", "状态", "", "状态",
						MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-IPCONF-INFO", "DELLEQUALLOGIC-IPCONF-INFO-6", "所属成员设备名称", "",
						"所属成员设备名称", MonitorItemType.TEXT),

				new MonitorItem("DELLEQUALLOGIC-MEMBER-INFO", "DELLEQUALLOGIC-MEMBER-INFO-1", "磁盘名称", "", "磁盘名称",
						MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-MEMBER-INFO", "DELLEQUALLOGIC-MEMBER-INFO-2", "理想状态", "", "理想状态",
						MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-MEMBER-INFO", "DELLEQUALLOGIC-MEMBER-INFO-3", "当前状态", "", "当前状态",
						MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-MEMBER-INFO", "DELLEQUALLOGIC-MEMBER-INFO-4", "型号", "", "型号",
						MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-MEMBER-INFO", "DELLEQUALLOGIC-MEMBER-INFO-5", "序列号", "", "序列号",
						MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-MEMBER-INFO", "DELLEQUALLOGIC-MEMBER-INFO-6", "成员设备磁盘数量", "个",
						"成员设备磁盘数量", MonitorItemType.NUMBER),
				new MonitorItem("DELLEQUALLOGIC-MEMBER-INFO", "DELLEQUALLOGIC-MEMBER-INFO-7", "磁盘总大小", "G", "磁盘总大小",
						MonitorItemType.NUMBER),
				new MonitorItem("DELLEQUALLOGIC-MEMBER-INFO", "DELLEQUALLOGIC-MEMBER-INFO-8", "已使用空间大小", "G",
						"已使用空间大小", MonitorItemType.NUMBER),
				new MonitorItem("DELLEQUALLOGIC-MEMBER-INFO", "DELLEQUALLOGIC-MEMBER-INFO-9", "RAID版本", "", "RAID版本",
						MonitorItemType.TEXT),

				new MonitorItem("DELLEQUALLOGIC-POWERSUPPLY-INFO", "DELLEQUALLOGIC-POWERSUPPLY-INFO-1", "电源名称", "",
						"电源名称", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-POWERSUPPLY-INFO", "DELLEQUALLOGIC-POWERSUPPLY-INFO-2", "电源状态", "",
						"电源状态", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-POWERSUPPLY-INFO", "DELLEQUALLOGIC-POWERSUPPLY-INFO-3", "所属成员设备名称", "",
						"所属成员设备名称", MonitorItemType.TEXT),

				new MonitorItem("DELLEQUALLOGIC-GROUP-INFO", "DELLEQUALLOGIC-GROUP-INFO-1", "群组信息", "", "群组信息",
						MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-GROUP-INFO", "DELLEQUALLOGIC-GROUP-INFO-2", "总成员数量", "个", "总成员数量",
						MonitorItemType.NUMBER),
				new MonitorItem("DELLEQUALLOGIC-GROUP-INFO", "DELLEQUALLOGIC-GROUP-INFO-3", "正在使用成员数量", "个",
						"正在使用成员数量", MonitorItemType.NUMBER),

				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-INFO", "DELLEQUALLOGIC-CONTORLLER-INFO-1", "控制器序列号", "",
						"控制器序列号", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-INFO", "DELLEQUALLOGIC-CONTORLLER-INFO-2", "控制器版本号", "",
						"控制器版本号", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-INFO", "DELLEQUALLOGIC-CONTORLLER-INFO-3", "主控制器或副控制器", "",
						"主控制器或副控制器", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-INFO", "DELLEQUALLOGIC-CONTORLLER-INFO-4", "控制器类型", "",
						"控制器类型", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-INFO", "DELLEQUALLOGIC-CONTORLLER-INFO-5", "处理器温度", "℃",
						"处理器温度", MonitorItemType.NUMBER),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-INFO", "DELLEQUALLOGIC-CONTORLLER-INFO-6", "芯片温度", "℃",
						"芯片温度", MonitorItemType.NUMBER),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-INFO", "DELLEQUALLOGIC-CONTORLLER-INFO-7", "电池状态", "",
						"电池状态", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-INFO", "DELLEQUALLOGIC-CONTORLLER-INFO-8", "所属成员设备名称", "",
						"所属成员设备名称", MonitorItemType.TEXT),

				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-CHIPTEMP-INFO", "DELLEQUALLOGIC-CONTORLLER-CHIPTEMP-INFO-1",
						"控制器序列号", "", "控制器序列号", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-CHIPTEMP-INFO", "DELLEQUALLOGIC-CONTORLLER-CHIPTEMP-INFO-2",
						"控制器序列号", "", "控制器版本号", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-CHIPTEMP-INFO", "DELLEQUALLOGIC-CONTORLLER-CHIPTEMP-INFO-3",
						"主控制器或副控制器", "", "主控制器或副控制器", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-CHIPTEMP-INFO", "DELLEQUALLOGIC-CONTORLLER-CHIPTEMP-INFO-4",
						"控制器类型", "", " 控制器类型", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-CHIPTEMP-INFO", "DELLEQUALLOGIC-CONTORLLER-CHIPTEMP-INFO-5",
						"控制器芯片温度", "℃", "控制器芯片温度", MonitorItemType.NUMBER),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-CHIPTEMP-INFO", "DELLEQUALLOGIC-CONTORLLER-CHIPTEMP-INFO-6",
						"电池状态", "", "电池状态", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-CHIPTEMP-INFO", "DELLEQUALLOGIC-CONTORLLER-CHIPTEMP-INFO-7",
						"所属成员设备名称", "", "所属成员设备名称", MonitorItemType.TEXT),

				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-PROCTEMP-INFO", "DELLEQUALLOGIC-CONTORLLER-PROCTEMP-INFO-1",
						"控制器序列号", "", "控制器序列号", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-PROCTEMP-INFO", "DELLEQUALLOGIC-CONTORLLER-PROCTEMP-INFO-2",
						"控制器版本号", "", "控制器版本号", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-PROCTEMP-INFO", "DELLEQUALLOGIC-CONTORLLER-PROCTEMP-INFO-3",
						"主控制器或副控制器", "", "主控制器或副控制器", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-PROCTEMP-INFO", "DELLEQUALLOGIC-CONTORLLER-PROCTEMP-INFO-4",
						"控制器类型", "", " 控制器类型", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-PROCTEMP-INFO", "DELLEQUALLOGIC-CONTORLLER-PROCTEMP-INFO-5",
						"控制器处理器温度", "℃", "芯片温度", MonitorItemType.NUMBER),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-PROCTEMP-INFO", "DELLEQUALLOGIC-CONTORLLER-PROCTEMP-INFO-6",
						"电池状态", "", "电池状态", MonitorItemType.TEXT),
				new MonitorItem("DELLEQUALLOGIC-CONTORLLER-PROCTEMP-INFO", "DELLEQUALLOGIC-CONTORLLER-PROCTEMP-INFO-7",
						"所属成员设备名称", "", "所属成员设备名称", MonitorItemType.TEXT), };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] {};
	}
}
