package com.broada.carrier.monitor.impl.virtual.fusioncompute;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.virtual.fusioncompute.cluster.ClusterMonitor;
import com.broada.carrier.monitor.impl.virtual.fusioncompute.host.HostMonitor;
import com.broada.carrier.monitor.impl.virtual.fusioncompute.vm.VmMonitor;
import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.method.fusioncompute.FusionComputeMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class FusionComputeMonitorPackage implements MonitorPackage {
	String[] targetTypeIds = new String[] { "Server" };
	String[] methodTypeIds = new String[] { FusionComputeMethod.TYPE_ID };
	int index = 1;

	@Override
	public MonitorType[] getTypes() {
		return new MonitorType[] {
//				new MonitorType("FUSIONCOMPUTE", "FusionCompute-Cluster", "FusionCompute集群监测", "监测FusionCompute集群信息",
//						MultiInstanceConfiger.class.getName(), ClusterMonitor.class.getName(), index++, targetTypeIds,
//						new String[] { FusionComputeMethod.TYPE_ID }),
				new MonitorType("FUSIONCOMPUTE", "FusionCompute-Host", "FusionCompute监测主机信息", "监测FusionCompute主机信息",
						MultiInstanceConfiger.class.getName(), HostMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),
				new MonitorType("FUSIONCOMPUTE", "FusionCompute-VM", "FusionCompute监测虚拟机信息", "监测FusionCompute虚拟机信息",
						MultiInstanceConfiger.class.getName(), VmMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds) };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("FusionCompute-Cluster", ClusterMonitor.CLUSTER_CPU_USAGE, "FusionCompute集群CPU占用率",
						"%", "FusionCompute集群CPU占用率", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Cluster", ClusterMonitor.CLUSTER_LOGIC_DISK_USAGE,
						"FusionCompute集群磁盘占用率", "%", "FusionCompute集群磁盘占用率", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Cluster", ClusterMonitor.CLUSTER_MEM_USAGE, "FusionCompute集群内存占用率", "%",
						"FusionCompute集群内存占用率", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Cluster", ClusterMonitor.CLUSTER_NIC_BYTE_IN, "FusionCompute集群网络流入流速",
						"", "FusionCompute集群网络流入流速", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Cluster", ClusterMonitor.CLUSTER_NIC_BYTE_IN_USAGE,
						"FusionCompute集群网络流入占用率", "%", "FusionCompute集群网络流入占用率", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Cluster", ClusterMonitor.CLUSTER_NIC_BYTE_OUT, "FusionCompute集群网络流出流速",
						"", "FusionCompute集群网络流出流速", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Cluster", ClusterMonitor.CLUSTER_NIC_BYTE_OUT_USAGE,
						"FusionCompute集群网络流出占用率", "%", "FusionCompute集群网络流出占用率", MonitorItemType.NUMBER),

				new MonitorItem("FusionCompute-Host", HostMonitor.HOST_IPADDR, "FusionCompute主机IP", "",
						"FusionCompute主机IP", MonitorItemType.TEXT),
				new MonitorItem("FusionCompute-Host", HostMonitor.HOST_CPU_USAGE, "FusionCompute主机CPU占用率", "%",
						"FusionCompute主机CPU占用率", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Host", HostMonitor.HOST_DISK_IO_IN, "FusionCompute主机磁盘I/O写入", "",
						"FusionCompute主机磁盘I/O写入", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Host", HostMonitor.HOST_DISK_IO_OUT, "FusionCompute主机磁盘I/O写出", "",
						"FusionCompute主机磁盘I/O写出", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Host", HostMonitor.HOST_DOMAIN_O_CPU_USAGE, "FusionCompute主机管理域CPU使用率",
						"%", "FusionCompute主机管理域CPU使用率", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Host", HostMonitor.HOST_DOMAIN_O_MEM_USAGE, "FusionCompute主机管理域内存使用率",
						"%", "FusionCompute主机管理域内存使用率", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Host", HostMonitor.HOST_DOMAIN_U_CPU_USAGE, "FusionCompute主机虚拟化域CPU使用率",
						"%", "FusionCompute主机虚拟化域CPU使用率", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Host", HostMonitor.HOST_DOMAIN_U_MEM_USAGE, "FusionCompute主机虚拟化域内存使用率",
						"%", "FusionCompute主机虚拟化域内存使用率", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Host", HostMonitor.HOST_LOGIC_DISK_USAGE, "FusionCompute主机磁盘使用率", "%",
						"FusionCompute主机磁盘使用率", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Host", HostMonitor.HOST_MEM_USAGE, "FusionCompute主机内存使用率", "%",
						"FusionCompute主机内存使用率", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Host", HostMonitor.HOST_NIC_BYTE_IN, "FusionCompute主机网络流入流速", "",
						"FusionCompute主机网络流入流速", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Host", HostMonitor.HOST_NIC_BYTE_IN_USAGE, "FusionCompute主机网络流入流速占用率",
						"%", "FusionCompute主机网络流入流速占用率", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Host", HostMonitor.HOST_NIC_BYTE_OUT, "FusionCompute主机网络流出流速", "",
						"FusionCompute主机网络流出流速", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Host", HostMonitor.HOST_NIC_BYTE_OUT_USAGE, "FusionCompute主机网络流出流速占用率",
						"%", "FusionCompute主机网络流出流速占用率", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Host", HostMonitor.HOST_NIC_PKG_RCV, "FusionCompute主机网络接受包速", "",
						"FusionCompute主机网络接受包速", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-Host", HostMonitor.HOST_NIC_PKG_SEND, "FusionCompute主机网络发送包速", "",
						"FusionCompute主机网络发送包速", MonitorItemType.NUMBER),

				new MonitorItem("FusionCompute-VM", VmMonitor.VM_OS, "FusionCompute虚拟机操作系统", "",
								"FusionCompute虚拟机操作系统", MonitorItemType.TEXT),
				new MonitorItem("FusionCompute-VM", VmMonitor.VM_IPADDR, "FusionCompute虚拟机Ip地址", "",
						"FusionCompute虚拟机Ip地址", MonitorItemType.TEXT),
				new MonitorItem("FusionCompute-VM", VmMonitor.VM_CPU_READY_TIME, "FusionCompute虚拟机CPU就绪时间", "",
						"FusionCompute虚拟机CPU就绪时间", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-VM", VmMonitor.VM_CPU_USAGE, "FusionCompute虚拟机CPU使用率", "",
						"FusionCompute虚拟机CPU使用率", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-VM", VmMonitor.VM_DISK_IO_IN, "FusionCompute虚拟机磁盘I/O写入", "",
						"FusionCompute虚拟机磁盘I/O写入", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-VM", VmMonitor.VM_DISK_IO_OUT, "FusionCompute虚拟机磁盘I/O读出", "",
						"FusionCompute虚拟机磁盘I/O读出", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-VM", VmMonitor.VM_DISK_IORD_TICKS, "FusionCompute虚拟机磁盘读延时", "",
						"FusionCompute虚拟机磁盘读延时", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-VM", VmMonitor.VM_DISK_IOWR_TICKS, "FusionCompute虚拟机磁盘写延时", "",
						"FusionCompute虚拟机磁盘写延时", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-VM", VmMonitor.VM_DISK_REQ_IN, "FusionCompute虚拟机磁盘每秒写请求次数", "次",
						"FusionCompute虚拟机磁盘每秒写请求次数", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-VM", VmMonitor.VM_DISK_REQ_OUT, "FusionCompute虚拟机磁盘每秒读请求次数", "次",
						"FusionComput虚拟机磁盘每秒读请求次数", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-VM", VmMonitor.VM_DISK_USAGE, "FusionCompute虚拟机磁盘使用率", "%",
						"FusionCompute虚拟机磁盘使用率", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-VM", VmMonitor.VM_MEM_USAGE, "FusionCompute虚拟机内存使用率", "%",
						"FusionCompute虚拟机内存使用率", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-VM", VmMonitor.VM_NIC_BYTE_IN, "FusionCompute虚拟机网络流入流速", "",
						"FusionCompute虚拟机网络流入流速", MonitorItemType.NUMBER),
				new MonitorItem("FusionCompute-VM", VmMonitor.VM_NIC_BYTE_OUT, "FusionCompute虚拟机网络流出流速", "",
						"FusionCompute虚拟机网络流出流速", MonitorItemType.NUMBER)

		};
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(FusionComputeMethod.TYPE_ID, "FusionCompute 监测协议",
				BaseMethodConfiger.class) };
	}

}
