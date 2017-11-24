package com.broada.carrier.monitor.impl.storage;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.generic.GenericConfiger;
import com.broada.carrier.monitor.impl.generic.GenericMonitor;
import com.broada.carrier.monitor.impl.storage.netapp.batteryStatus.BatteryStatusMonitor;
import com.broada.carrier.monitor.impl.storage.netapp.cpu.CPUMonitor;
import com.broada.carrier.monitor.impl.storage.netapp.cpuidletime.CPUIDletimeMonitor;
import com.broada.carrier.monitor.impl.storage.netapp.cpuinterrup.CPUInterrupMonitor;
import com.broada.carrier.monitor.impl.storage.netapp.failedPower.FailedPowerMonitor;
import com.broada.carrier.monitor.impl.storage.netapp.fsStatus.FsStatusMonitor;
import com.broada.carrier.monitor.impl.storage.netapp.fsUsed.FsUsedMonitor;
import com.broada.carrier.monitor.impl.storage.netapp.info.SnmpHostInfoMonitor;
import com.broada.carrier.monitor.impl.storage.netapp.raid.RaidStatusMonitor;

import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.smis.SmisMethod;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.method.snmp.SnmpMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class NETAPPFASSNMPMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "DiskArrayNETAPP" };
		String[] methodTypeIds = new String[] {  SnmpMethod.TYPE_ID};
		int index = 1;

		return new MonitorType[] {
				new MonitorType("NETAPP_FAS_SNMP","NETAPPFAS-BATTERYSTATUS", "NETAPP电池状态(SNMP)",
						"通过SNMP方式采集NETAPP电池状态。",
						SingleInstanceConfiger.class.getName(),
						BatteryStatusMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("NETAPP_FAS_SNMP","NETAPPFAS-CPU", "NETAPPCPU使用率(SNMP)",
						"使用SNMP协议监测NETAPPCPU的使用情况。",
						SingleInstanceConfiger.class.getName(),
						CPUMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),

				new MonitorType("NETAPP_FAS_SNMP","NETAPPFAS-CPUIDLETIME", "NETAPP CPU闲置率(SNMP)",
						"使用Snmp协议监测NETAPP CPU闲置率。",
						MultiInstanceConfiger.class.getName(),
						CPUIDletimeMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("NETAPP_FAS_SNMP","NETAPPFAS-CPUINTERRUP", "NETAPP CPU中断率(SNMP)",
						"使用Snmp协议监测NETAPP CPU中断率。",
						MultiInstanceConfiger.class.getName(),
						CPUInterrupMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("NETAPP_FAS_SNMP","NETAPPFAS-FAILDPOWER", "NETAPP问题电源数(SNMP)",
						"使用Snmp协议监测NETAPP问题电源数。",
						MultiInstanceConfiger.class.getName(),
						FailedPowerMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("NETAPP_FAS_SNMP","NETAPPFAS-FSSTATUS", "NETAPP文件系统状态(SNMP)",
						"使用SNMP协议监测NETAPP文件系统状态。",
						SingleInstanceConfiger.class.getName(),
						FsStatusMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("NETAPP_FAS_SNMP","NETAPPFAS-FSUSED", "NETAPP文件系统空间使用率(SNMP)",
						"使用SNMP协议监测NETAPP文件系统空间使用率。",
						SingleInstanceConfiger.class.getName(),
						FsUsedMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),

				new MonitorType("NETAPP_FAS_SNMP","NETAPPFAS-BASEINFO", "NETAPP基本信息采集(SNMP)",
						"通过SNMP方式采集NETAPP的基本信息。",
						SingleInstanceConfiger.class.getName(),
						SnmpHostInfoMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("NETAPP_FAS_SNMP","NETAPPFAS-RAID", "NETAPP磁盘驱动器状态(SNMP)",
						"使用SNMP协议监测NETAPP磁盘驱动器状态。",
						SingleInstanceConfiger.class.getName(),
						RaidStatusMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds) };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("NETAPPFAS-BATTERYSTATUS", "NETAPPFAS-BATTERY-STATUS-1", "电池状态", "",
						"NETAPP电池状态", MonitorItemType.TEXT),

				new MonitorItem("NETAPPFAS-CPU","NETAPPFAS-CPU-1", "CPU使用率", "%", "CPU使用率",
						MonitorItemType.NUMBER),
				new MonitorItem("NETAPPFAS-CPU","NETAPPFAS-CPU-2", "CPU中断率", "%", "CPU中断率",
						MonitorItemType.NUMBER),
				new MonitorItem("NETAPPFAS-CPU","NETAPPFAS-CPU-3", "CPU闲置率", "%", "CPU闲置率",
						MonitorItemType.NUMBER),
				new MonitorItem("NETAPPFAS-CPU","NETAPPFAS-CPU-4", "CPU启动时间", "", "CPU启动时间",
						MonitorItemType.TEXT),

				new MonitorItem("NETAPPFAS-CPUIDLETIME", "NETAPPFAS-CPUIDLETIME-1", "CPU闲置率", "%",
						"CPU闲置率", MonitorItemType.NUMBER),

				new MonitorItem("NETAPPFAS-CPUINTERRUP", "NETAPPFAS-CPUINTERRUP-1", "CPU中断率", "%",
						"CPU中断率", MonitorItemType.NUMBER),

				new MonitorItem("NETAPPFAS-FAILDPOWER","NETAPPFAS-FAILEDPOWER-1", "问题电源数", "个", "问题电源数",
						MonitorItemType.NUMBER),

				new MonitorItem("NETAPPFAS-FSSTATUS", "NETAPPFAS-FSSTATUS-1", "文件系统状态", "", "文件系统状态",
						MonitorItemType.TEXT),

				new MonitorItem("NETAPPFAS-FSUSED","NETAPPFAS-FSUSED-1", "文件系统空间使用率", "%",
						"文件系统空间使用百分比", MonitorItemType.NUMBER),

				new MonitorItem("NETAPPFAS-BASEINFO","NETAPPFAS-BASEINFO-1", "设备概况", "", "设备概况",
						MonitorItemType.TEXT),
				new MonitorItem("NETAPPFAS-BASEINFO","NETAPPFAS-BASEINFO-2", "主机名", "", "主机名",
						MonitorItemType.TEXT),
				new MonitorItem("NETAPPFAS-BASEINFO","NETAPPFAS-BASEINFO-3", "所在地址", "", "所在地址",
						MonitorItemType.TEXT),

				new MonitorItem("NETAPPFAS-RAID","NETAPPFAS-RAIDSTATUS-1", "raid状态", "", "raid状态",
						MonitorItemType.TEXT) };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] {};
	}
}
