package com.broada.carrier.monitor.impl.virtual.fusionmanager.top;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.virtual.fusionmanager.top.vm.VmMonitor;
import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.method.fusionmanager.FusionManagerMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class FMTopMonitorPackage implements MonitorPackage {
	String[] targetTypeIds = new String[] { "Server" };
	String[] methodTypeIds = new String[] { FusionManagerMethod.TYPE_ID };
	int index = 1;

	@Override
	public MonitorType[] getTypes() {
		return new MonitorType[] { new MonitorType("FUSIONMANAGER_TOP", "FUSIONMANAGER_TOP-VM",
				"FUSIONMANAGER_TOP监测虚拟机信息", "监测FUSIONMANAGER_TOP虚拟机信息", MultiInstanceConfiger.class.getName(),
				VmMonitor.class.getName(), index++, targetTypeIds, methodTypeIds) };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("FUSIONMANAGER_TOP-VM", VmMonitor.VM_OS, "FUSIONMANAGER_TOP虚拟机操作系统",
						"", "FUSIONMANAGER_TOP虚拟机操作系统", MonitorItemType.TEXT),
				new MonitorItem("FUSIONMANAGER_TOP-VM", VmMonitor.VM_CPU_READY_TIME, "FUSIONMANAGER_TOP虚拟机CPU就绪时间",
						"ms", "FUSIONMANAGER_TOP虚拟机CPU就绪时间", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_TOP-VM", VmMonitor.VM_CPU_USAGE, "FUSIONMANAGER_TOP虚拟机CPU使用率", "%",
						"FUSIONMANAGER_TOP虚拟机CPU使用率", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_TOP-VM", VmMonitor.VM_DISK_IO_IN, "FUSIONMANAGER_TOP虚拟机磁盘I/O写入", "KB/s",
						"FUSIONMANAGER_TOP虚拟机磁盘I/O写入", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_TOP-VM", VmMonitor.VM_DISK_IO_OUT, "FUSIONMANAGER_TOP虚拟机磁盘I/O读出", "KB/s",
						"FUSIONMANAGER_TOP虚拟机磁盘I/O读出", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_TOP-VM", VmMonitor.VM_DISK_USAGE, "FUSIONMANAGER_TOP虚拟机磁盘使用率", "%",
						"FUSIONMANAGER_TOP虚拟机磁盘使用率", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_TOP-VM", VmMonitor.VM_MEM_USAGE, "FUSIONMANAGER_TOP虚拟机内存使用率", "%",
						"FUSIONMANAGER_TOP虚拟机内存使用率", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_TOP-VM", VmMonitor.VM_NIC_BYTE_IN, "FUSIONMANAGER_TOP虚拟机网络流入流速", "KB/s",
						"FUSIONMANAGER_TOP虚拟机网络流入流速", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_TOP-VM", VmMonitor.VM_NIC_BYTE_OUT, "FUSIONMANAGER_TOP虚拟机网络流出流速", "KB/s",
						"FUSIONMANAGER_TOP虚拟机网络流出流速", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_TOP-VM", VmMonitor.VM_DISK_IN_PS, "FUSIONMANAGER_TOP虚拟机磁盘I/O写命令次数每秒",
						"次/s", "FUSIONMANAGER_TOP虚拟机磁盘I/O写命令次数每秒", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_TOP-VM", VmMonitor.VM_DISK_OUT_PS, "FUSIONMANAGER_TOP虚拟机磁盘I/O读命令次数每秒",
						"次/s", "FUSIONMANAGER_TOP虚拟机磁盘I/O读命令次数每秒", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_TOP-VM", VmMonitor.VM_DISK_WRITE_DELAY, "FUSIONMANAGER_TOP虚拟机磁盘写时延",
						"ms", "FUSIONMANAGER_TOP虚拟机磁盘写时延", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_TOP-VM", VmMonitor.VM_DISK_READ_DELAY, "FUSIONMANAGER_TOP虚拟机磁盘写时延",
						"ms", "FUSIONMANAGER_TOP虚拟机磁盘写时延", MonitorItemType.NUMBER)

		};
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(FusionManagerMethod.TYPE_ID, "FUSIONMANAGER_TOP 监测协议",
				BaseMethodConfiger.class) };
	}

}
