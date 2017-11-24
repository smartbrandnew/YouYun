package com.broada.carrier.monitor.impl.virtual.vmware.vcenter.vm;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.virtual.vmware.vcenter.vm.cpu.VCenterVmCPUMonitor;
import com.broada.carrier.monitor.impl.virtual.vmware.vcenter.vm.disk.VCenterVmDiskMonitor;
import com.broada.carrier.monitor.impl.virtual.vmware.vcenter.vm.info.VCenterVmInfoMonitor;
import com.broada.carrier.monitor.impl.virtual.vmware.vcenter.vm.network.VCenterVmNetworkMonitor;
import com.broada.carrier.monitor.impl.virtual.vmware.vcenter.vm.ram.VCenterVmRAMMonitor;
import com.broada.carrier.monitor.method.vmware.VSphereMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class VmwareMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "Server" };
		String[] methodTypeIds = new String[] { VSphereMonitorMethodOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("VMWARE", "VMWARE-VM", "vCenter VM 基本信息监测 [可用性]", "使用web service sdk监测虚拟机基本信息。",
						MultiInstanceConfiger.class.getName(), VCenterVmInfoMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("VMWARE", "VMWARE-VM-CPU", "vCenter VM CPU监测", "使用web service sdk监测虚拟机的CPU使用情况。",
						MultiInstanceConfiger.class.getName(), VCenterVmCPUMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("VMWARE", "VMWARE-VM-DISK", "vCenter VM DISK监测", "使用web service sdk监测虚拟机的磁盘使用信息。",
						MultiInstanceConfiger.class.getName(), VCenterVmDiskMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("VMWARE", "VMWARE-VM-RAM", "vCenter VM RAM监测", "使用web service sdk监测虚拟机的内存使用情况。",
						MultiInstanceConfiger.class.getName(), VCenterVmRAMMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("VMWARE", "VMWARE-VM-NET", "vCenter VM NET监测", "使用web service sdk监测虚拟机的网络使用情况。",
						MultiInstanceConfiger.class.getName(), VCenterVmNetworkMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds) };
	}

	@Override
	public MonitorItem[] getItems() {

		return new MonitorItem[] {
				new MonitorItem("VMWARE-VM", "ESX-BASE-1", "主机信息", "", "主机的基本信息或状态", MonitorItemType.TEXT),
				new MonitorItem("VMWARE-VM", "esx-vm-compute-resource-name", "VM所在计算资源名称", "", "VM所在计算资源名称", MonitorItemType.TEXT),
				new MonitorItem("VMWARE-VM", "esx-vm-compute-resource-path", "VM所在计算资源路径", "", "VM所在计算资源路径", MonitorItemType.TEXT),
				new MonitorItem("VMWARE-VM", "esx-vm-compute-resource-type", "VM所在计算资源类型", "", "VM所在计算资源类型", MonitorItemType.TEXT),
				new MonitorItem("VMWARE-VM", "esx-vm-guest-full-name", "VM配置操作系统", "", "VM的配置操作系统", MonitorItemType.TEXT),
				new MonitorItem("VMWARE-VM", "esx-vm-hyper-uuid", "主机UUID", "", "VM所在主机的uuid", MonitorItemType.TEXT),
				new MonitorItem("VMWARE-VM", "esx-vm-over-all-status", "VM告警状态", "", "VM告警状态", MonitorItemType.TEXT),
				new MonitorItem("VMWARE-VM", "esx-vm-path", "VM路径", "", "VM的唯一路径", MonitorItemType.TEXT),
				new MonitorItem("VMWARE-VM", "esx-vm-path-name", "VM文件路径", "", "VM的配置文件所存路径", MonitorItemType.TEXT),
				new MonitorItem("VMWARE-VM", "esx-vm-power-state", "VM电源状态", "", "VM电源状态", MonitorItemType.TEXT),
				new MonitorItem("VMWARE-VM", "esx-vm-net-ip-address", "VM IP地址", "", "VM各个网卡的ip地址(须安装VMWARE TOOLS)",
						MonitorItemType.TEXT),
				new MonitorItem("VMWARE-VM", "esx-vm-is-template", "是否为模板", "", "是否为模板", MonitorItemType.TEXT),
				//cpu数量，放在基本信息里为了同步性能指标的时候能够同步资源
				new MonitorItem("VMWARE-VM", "esx-vm-cpu-num", "cpu数量", "个", "cpu数量", MonitorItemType.NUMBER),
				
				new MonitorItem("VMWARE-VM-CPU", "ESX-VM-CPU-1", "名称", "", "虚拟机名称", MonitorItemType.TEXT),
				new MonitorItem("VMWARE-VM-CPU", "ESX-VM-CPU-3", "CPU频率", "Hz", "虚拟机的CPU频率", MonitorItemType.NUMBER),
				new MonitorItem("VMWARE-VM-CPU", "ESX-VM-CPU-4", "CPU使用率", "%", "虚拟机的CPU使用率", MonitorItemType.NUMBER),

				new MonitorItem("VMWARE-VM-DISK", "ESX-VM-DISK-1", "名称", "", "虚拟机名称", MonitorItemType.TEXT),
				new MonitorItem("VMWARE-VM-DISK", "ESX-VM-DISK-2", "存储置备", "MB", "存储置备大小", MonitorItemType.NUMBER),
				new MonitorItem("VMWARE-VM-DISK", "ESX-VM-DISK-3", "已分配", "MB", "已分配大小", MonitorItemType.NUMBER),
				new MonitorItem("VMWARE-VM-DISK", "ESX-VM-DISK-4", "未共享", "MB", "未共享大小", MonitorItemType.NUMBER),
				new MonitorItem("VMWARE-VM-DISK", "ESX-VM-DISK-5", "使用率", "%", "已分配使用率", MonitorItemType.NUMBER),

				new MonitorItem("VMWARE-VM-RAM", "ESX-VM-RAM-1", "名称", "", "虚拟机名称", MonitorItemType.TEXT),
				new MonitorItem("VMWARE-VM-RAM", "ESX-VM-RAM-2", "已使用", "MB", "虚拟机可已使用的内存量", MonitorItemType.NUMBER),
				new MonitorItem("VMWARE-VM-RAM", "ESX-VM-RAM-3", "总量", "MB", "虚拟机的内存总量", MonitorItemType.NUMBER),
				new MonitorItem("VMWARE-VM-RAM", "ESX-VM-RAM-4", "内存使用率", "%", "虚拟机的内存使用率", MonitorItemType.NUMBER),
				
				new MonitorItem("VMWARE-VM-NET", "ESX-VM-NET-1", "接受速率", "Kbps", "平均接受速率", MonitorItemType.NUMBER),
				new MonitorItem("VMWARE-VM-NET", "ESX-VM-NET-2", "发送速率", "Kbps", "平均发送速率", MonitorItemType.NUMBER),
				new MonitorItem("VMWARE-VM-NET", "ESX-VM-NET-3", "接受包转发率", "pps", "平均接受包转发率", MonitorItemType.NUMBER),
				new MonitorItem("VMWARE-VM-NET", "ESX-VM-NET-4", "发送包转发率", "pps", "平均发送包转发率", MonitorItemType.NUMBER),
				new MonitorItem("VMWARE-VM-NET", "ESX-VM-NET-5", "接受丢包率", "pps", "平均接受丢包率", MonitorItemType.NUMBER),
				new MonitorItem("VMWARE-VM-NET", "ESX-VM-NET-6", "发送丢包率", "pps", "平均发送丢包率", MonitorItemType.NUMBER),
				new MonitorItem("VMWARE-VM-NET", "ESX-VM-NET-7", "接受误包率", "pps", "平均接受误包率", MonitorItemType.NUMBER),
				new MonitorItem("VMWARE-VM-NET", "ESX-VM-NET-8", "发送误包率", "pps", "平均发送误包率", MonitorItemType.NUMBER) };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		// TODO Auto-generated method stub
		return null;
	}

}
