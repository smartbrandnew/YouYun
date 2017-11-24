package com.broada.carrier.monitor.impl.host.snmp;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.host.snmp.cpu.HostCpuMonitor;
import com.broada.carrier.monitor.impl.host.snmp.disk.DISKMonitor;
import com.broada.carrier.monitor.impl.host.snmp.info.SnmpHostInfoMonitor;
import com.broada.carrier.monitor.impl.host.snmp.process.ProcessMonitor;
import com.broada.carrier.monitor.impl.host.snmp.ram.HostRamMonitor;
import com.broada.carrier.monitor.impl.host.snmp.winservice.WinServiceMonitor;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.method.snmp.SnmpMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class HostSnmpMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "OS" };
		String[] targetTypeIdsWIN = new String[] { "Windows" };
		String[] methodTypeIds = new String[] { SnmpMethod.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("SNMP", "SNMP-HOSTINFO", "主机基本信息采集(SNMP)", "通过SNMP方式采集主机的基本信息和运行状态,如果无法采集则告警。",
						SingleInstanceConfiger.class.getName(), SnmpHostInfoMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("SNMP", "SNMP-HOSTCPU", "主机CPU使用率", "使用SNMP协议监测主机CPU的使用情况,并记录性能数据,超过设定的阈值产生告警。",
						SingleInstanceConfiger.class.getName(), HostCpuMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),

				new MonitorType("SNMP", "SNMP-WINSERVICE", "Windows服务监控", "监测Windows服务工作状态是否正常。",
						MultiInstanceConfiger.class.getName(), WinServiceMonitor.class.getName(), index++,
						targetTypeIdsWIN, methodTypeIds),

				new MonitorType("SNMP", "SNMP-PROCESS", "应用进程", "使用Snmp协议监测指定的主机的指定进程的运行情况，然后与设定的值比较，判断进程运行是否正常。",
						MultiInstanceConfiger.class.getName(), ProcessMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),

				new MonitorType("SNMP", "SNMP-DISK", "磁盘使用率", "使用Snmp协议监测指定的节点的磁盘使用情况，可以监测所有分区或指定分区的使用百分比。",
						MultiInstanceConfiger.class.getName(), DISKMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),

				new MonitorType("SNMP", "SNMP-HOSTRAM", "主机内存使用率", "使用SNMP协议监测主机内存的使用情况,并记录性能数据,超过设定的阈值产生告警。",
						SingleInstanceConfiger.class.getName(), HostRamMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds), };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("SNMP-HOSTCPU", "SNMP-HOSTCPU-1", "主机CPU使用率", "%", "主机所有进程的CPU使用率总和", MonitorItemType.NUMBER),

				new MonitorItem("SNMP-HOSTINFO", "SNMP-HOSTINFO-1", "操作系统", "", "操作系统版本信息", MonitorItemType.TEXT),
				new MonitorItem("SNMP-HOSTINFO", "SNMP-HOSTINFO-2", "机器型号", "", "机器型号", MonitorItemType.TEXT),
				new MonitorItem("SNMP-HOSTINFO", "SNMP-HOSTINFO-3", "主机名", "", "主机名", MonitorItemType.TEXT),
				new MonitorItem("SNMP-HOSTINFO", "SNMP-HOSTINFO-4", "MAC地址", "", "主机所在的物理位置", MonitorItemType.TEXT),
				new MonitorItem("SNMP-HOSTINFO", "SNMP-HOSTINFO-5", "内存大小", "MB", "主机的内存总数大小", MonitorItemType.NUMBER),
				new MonitorItem("SNMP-HOSTINFO", "SNMP-HOSTINFO-6", "CPU核数", "个", "主机CPU内核数", MonitorItemType.NUMBER),
				new MonitorItem("SNMP-HOSTINFO", "SNMP-HOSTINFO-7", "端口列表", "", "主机物理端口列表", MonitorItemType.TEXT),

				new MonitorItem("SNMP-WINSERVICE","SNMP-WINSERVICE-1", "运行状态", "", "服务的运行状态", MonitorItemType.TEXT),

				new MonitorItem( "SNMP-DISK","SNMP-DISK-1", "磁盘使用率", "%", "指定分区已使用的空间和分区总空间的百分比", MonitorItemType.NUMBER),
				new MonitorItem( "SNMP-DISK","SNMP-DISK-2", "未使用空间", "MB", "指定分区的未使用空间", MonitorItemType.NUMBER),
				new MonitorItem( "SNMP-DISK","SNMP-DISK-3", "分区总空间", "MB", "指定分区的总空间", MonitorItemType.NUMBER),

				new MonitorItem("SNMP-PROCESS", "SNMP-PROCESS-1", "内存使用量", "MB", "指定进程的内存使用大小", MonitorItemType.NUMBER),
				new MonitorItem("SNMP-PROCESS", "SNMP-PROCESS-2", "内存使用率", "%", "指定进程的内存使用大小占总内存的百分比", MonitorItemType.NUMBER),
				new MonitorItem("SNMP-PROCESS", "SNMP-PROCESS-3", "CPU使用率", "%", "指定进程的CPU使用时间占该时间片的百分比", MonitorItemType.NUMBER),
				new MonitorItem("SNMP-PROCESS", "SNMP-PROCESS-4", "工作状态", "", "进程运行状态", MonitorItemType.TEXT),

				new MonitorItem("SNMP-HOSTRAM","SNMP-HOSTRAM-1", "内存使用量", "MB", "主机所有进程的内存使用量总和", MonitorItemType.NUMBER),
				new MonitorItem("SNMP-HOSTRAM","SNMP-HOSTRAM-2", "内存使用率", "%", "主机所有进程的内存使用率总和", MonitorItemType.NUMBER), };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(SnmpMethod.TYPE_ID, "SNMP监测协议", SnmpMethodConfiger.class), };
	}
}
