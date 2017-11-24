package com.broada.carrier.monitor.impl.virtual.fusionmanager.local;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.virtual.fusionmanager.local.cluster.ClusterMonitor;
import com.broada.carrier.monitor.impl.virtual.fusionmanager.local.host.HostMonitor;
import com.broada.carrier.monitor.impl.virtual.fusionmanager.local.vm.VmMonitor;
import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.method.fusionmanager.FusionManagerMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class FMLocalMonitorPackage implements MonitorPackage {
	String[] targetTypeIds = new String[] { "Server" };
	String[] methodTypeIds = new String[] { FusionManagerMethod.TYPE_ID };
	int index = 1;

	@Override
	public MonitorType[] getTypes() {
		return new MonitorType[] {
				new MonitorType("FUSIONMANAGER_LOCAL", "FUSIONMANAGER_LOCAL-Cluster", "FUSIONMANAGER_LOCAL集群监测",
						"监测FUSIONMANAGER_LOCAL集群信息", MultiInstanceConfiger.class.getName(),
						ClusterMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				new MonitorType("FUSIONMANAGER_LOCAL", "FUSIONMANAGER_LOCAL-Host", "FUSIONMANAGER_LOCAL监测主机信息",
						"监测FUSIONMANAGER_LOCAL主机信息", MultiInstanceConfiger.class.getName(),
						HostMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				new MonitorType("FUSIONMANAGER_LOCAL", "FUSIONMANAGER_LOCAL-VM", "FUSIONMANAGER_LOCAL监测虚拟机信息",
						"监测FUSIONMANAGER_LOCAL虚拟机信息", MultiInstanceConfiger.class.getName(), VmMonitor.class.getName(),
						index++, targetTypeIds, methodTypeIds), };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_CPU_USAGE,
						"FUSIONMANAGER_LOCAL集群CPU占用率", "%", "FUSIONMANAGER_LOCAL集群CPU占用率", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_STORAGE__USAGE,
						"FUSIONMANAGER_LOCAL集群存储占用率", "%", "FUSIONMANAGER_LOCAL集群存储占用率", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_MEM_USAGE,
						"FUSIONMANAGER_LOCAL集群内存占用率", "%", "FUSIONMANAGER_LOCAL集群内存占用率", MonitorItemType.NUMBER),

				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_CPU_CAPACITY_ALLOCATED,
						"FUSIONMANAGER_LOCAL集群已分配cpu容量", "GHZ", "FUSIONMANAGER_LOCAL集群已分配cpu容量", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_CPU_CAPACITY_AVALIABLE,
						"FUSIONMANAGER_LOCAL集群可用cpu容量", "GHZ", "FUSIONMANAGER_LOCAL集群可用cpu容量", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_CPU_CAPACITY_OTALCAPACITY,
						"FUSIONMANAGER_LOCAL集群cpu总容量", "GHZ", "FUSIONMANAGER_LOCAL集群cpu总容量", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_CPU_CAPACITY_RESERVECAPACITY,
						"FUSIONMANAGER_LOCAL集群cpu预留容量", "GHZ", "FUSIONMANAGER_LOCAL集群cpu预留容量", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_CPU_CAPACITY_USEDCAPACITY,
						"FUSIONMANAGER_LOCAL集群已用cpu容量", "GHZ", "FUSIONMANAGER_LOCAL集群已用cpu容量", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_MEM_CAPACITY_ALLOCATED,
						"FUSIONMANAGER_LOCAL集群已分配内存容量", "GB", "FUSIONMANAGER_LOCAL集群已分配内存容量", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_MEM_CAPACITY_AVALIABLE,
						"FUSIONMANAGER_LOCAL集群可用内存容量", "GB", "FUSIONMANAGER_LOCAL集群可用内存容量", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_MEM_CAPACITY_OTALCAPACITY,
						"FUSIONMANAGER_LOCAL集群内存总容量", "GB", "FUSIONMANAGER_LOCAL集群内存总容量", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_MEM_CAPACITY_RESERVECAPACITY,
						"FUSIONMANAGER_LOCAL集群内存预留容量", "GB", "FUSIONMANAGER_LOCAL集群内存预留容量", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_MEM_CAPACITY_USEDCAPACITY,
						"FUSIONMANAGER_LOCAL集群已用内存容量", "GB", "FUSIONMANAGER_LOCAL集群已用内存容量", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_STORAGE_CAPACITY_ALLOCATED,
						"FUSIONMANAGER_LOCAL集群已分配存储容量", "GB", "FUSIONMANAGER_LOCAL集群已分配存储容量", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_STORAGE_CAPACITY_AVALIABLE,
						"FUSIONMANAGER_LOCAL集群可用存储容量", "GB", "FUSIONMANAGER_LOCAL集群可用存储容量", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_STORAGE_CAPACITY_OTALCAPACITY,
						"FUSIONMANAGER_LOCAL集群存储总容量", "GB", "FUSIONMANAGER_LOCAL集群存储总容量", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_STORAGE_CAPACITY_RESERVECAPACITY,
						"FUSIONMANAGER_LOCAL集群存储预留容量", "GB", "FUSIONMANAGER_LOCAL集群存储预留容量", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Cluster", ClusterMonitor.CLUSTER_STORAGE_CAPACITY_USEDCAPACITY,
						"FUSIONMANAGER_LOCAL集群已用存储容量", "GB", "FUSIONMANAGER_LOCAL集群已用存储容量", MonitorItemType.NUMBER),

				new MonitorItem("FUSIONMANAGER_LOCAL-Host", HostMonitor.HOST_IPADDR, "FUSIONMANAGER_LOCAL主机IP", "",
						"FUSIONMANAGER_LOCAL主机IP", MonitorItemType.TEXT),
				new MonitorItem("FUSIONMANAGER_LOCAL-Host", HostMonitor.HOST_CPU_USAGE, "FUSIONMANAGER_LOCAL主机CPU占用率",
						"%", "FUSIONMANAGER_LOCAL主机CPU占用率", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Host", HostMonitor.HOST_DISK_IO_IN,
						"FUSIONMANAGER_LOCAL主机磁盘I/O写入", "KB/s", "FUSIONMANAGER_LOCAL主机磁盘I/O写入", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Host", HostMonitor.HOST_DISK_IO_OUT,
						"FUSIONMANAGER_LOCAL主机磁盘I/O写出", "KB/s", "FUSIONMANAGER_LOCAL主机磁盘I/O写出", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Host", HostMonitor.HOST_MEM_USAGE, "FUSIONMANAGER_LOCAL主机内存使用率",
						"%", "FUSIONMANAGER_LOCAL主机内存使用率", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Host", HostMonitor.HOST_NIC_BYTE_IN,
						"FUSIONMANAGER_LOCAL主机网络流入流速", "KB/s", "FUSIONMANAGER_LOCAL主机网络流入流速", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Host", HostMonitor.HOST_NIC_BYTE_OUT,
						"FUSIONMANAGER_LOCAL主机网络流出流速", "KB/s", "FUSIONMANAGER_LOCAL主机网络流出流速", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Host", HostMonitor.HOST_DISK_USAGE, "FUSIONMANAGER_LOCAL主机磁盘使用率",
						"%", "FUSIONMANAGER_LOCAL主机磁盘使用率", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Host", HostMonitor.HOST_NET_RECEIVE_PKG_RATE,
						"FUSIONMANAGER_LOCAL主机网络接受包速", "次/s", "FUSIONMANAGER_LOCAL主机网络接受包速", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-Host", HostMonitor.HOST_NET_SEND_PKG_RATE,
						"FUSIONMANAGER_LOCAL主机网络发送包速", "次/s", "FUSIONMANAGER_LOCAL主机网络发送包速", MonitorItemType.NUMBER),

				new MonitorItem("FUSIONMANAGER_LOCAL-VM", VmMonitor.VM_OS, "FUSIONMANAGER_LOCAL虚拟机操作系统", "",
						"FUSIONMANAGER_LOCAL虚拟机操作系统", MonitorItemType.TEXT),
				new MonitorItem("FUSIONMANAGER_LOCAL-VM", VmMonitor.VM_IPADDR, "FUSIONMANAGER_LOCAL虚拟机Ip地址", "",
						"FUSIONMANAGER_LOCAL虚拟机Ip地址", MonitorItemType.TEXT),
				new MonitorItem("FUSIONMANAGER_LOCAL-VM", VmMonitor.VM_CPU_READY_TIME, "FUSIONMANAGER_LOCAL虚拟机CPU就绪时间",
						"ms", "FUSIONMANAGER_LOCAL虚拟机CPU就绪时间", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-VM", VmMonitor.VM_CPU_USAGE, "FUSIONMANAGER_LOCAL虚拟机CPU使用率", "%",
						"FUSIONMANAGER_LOCAL虚拟机CPU使用率", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-VM", VmMonitor.VM_DISK_IO_IN, "FUSIONMANAGER_LOCAL虚拟机磁盘I/O写入",
						"KB/s", "FUSIONMANAGER_LOCAL虚拟机磁盘I/O写入", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-VM", VmMonitor.VM_DISK_IO_OUT, "FUSIONMANAGER_LOCAL虚拟机磁盘I/O读出",
						"KB/s", "FUSIONMANAGER_LOCAL虚拟机磁盘I/O读出", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-VM", VmMonitor.VM_DISK_USAGE, "FUSIONMANAGER_LOCAL虚拟机磁盘使用率", "%",
						"FUSIONMANAGER_LOCAL虚拟机磁盘使用率", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-VM", VmMonitor.VM_MEM_USAGE, "FUSIONMANAGER_LOCAL虚拟机内存使用率", "%",
						"FUSIONMANAGER_LOCAL虚拟机内存使用率", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-VM", VmMonitor.VM_NIC_BYTE_IN, "FUSIONMANAGER_LOCAL虚拟机网络流入流速",
						"KB/s", "FUSIONMANAGER_LOCAL虚拟机网络流入流速", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-VM", VmMonitor.VM_NIC_BYTE_OUT, "FUSIONMANAGER_LOCAL虚拟机网络流出流速",
						"KB/s", "FUSIONMANAGER_LOCAL虚拟机网络流出流速", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-VM", VmMonitor.VM_DISK_IN_PS,
						"FUSIONMANAGER_LOCAL虚拟机磁盘I/O写命令次数每秒", "次/s", "FUSIONMANAGER_LOCAL虚拟机磁盘I/O写命令次数每秒",
						MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-VM", VmMonitor.VM_DISK_OUT_PS,
						"FUSIONMANAGER_LOCAL虚拟机磁盘I/O读命令次数每秒", "次/s", "FUSIONMANAGER_LOCAL虚拟机磁盘I/O读命令次数每秒",
						MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-VM", VmMonitor.VM_DISK_WRITE_DELAY, "FUSIONMANAGER_LOCAL虚拟机磁盘写时延",
						"ms", "FUSIONMANAGER_LOCAL虚拟机磁盘写时延", MonitorItemType.NUMBER),
				new MonitorItem("FUSIONMANAGER_LOCAL-VM", VmMonitor.VM_DISK_READ_DELAY, "FUSIONMANAGER_LOCAL虚拟机磁盘写时延",
						"ms", "FUSIONMANAGER_LOCAL虚拟机磁盘写时延", MonitorItemType.NUMBER)

		};
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(FusionManagerMethod.TYPE_ID, "FUSIONMANAGER_LOCAL 监测协议",
				BaseMethodConfiger.class) };
	}

}
