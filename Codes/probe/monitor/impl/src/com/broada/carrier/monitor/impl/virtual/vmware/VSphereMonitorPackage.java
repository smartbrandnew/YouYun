package com.broada.carrier.monitor.impl.virtual.vmware;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.virtual.vmware.vcenter.hypervisor.cpu.VCenterHypervisorCPUMonitor;
import com.broada.carrier.monitor.impl.virtual.vmware.vcenter.hypervisor.info.VCenterHypervisorInfoMonitor;
import com.broada.carrier.monitor.impl.virtual.vmware.vcenter.hypervisor.ram.VCenterHypervisorRAMMonitor;
import com.broada.carrier.monitor.method.vmware.VSphereConfPanel;
import com.broada.carrier.monitor.method.vmware.VSphereMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class VSphereMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "Server" };
		String[] methodTypeIds = new String[] { VSphereMonitorMethodOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("HYPERVISOR", "HYPERVISOR-VCENTER-CPU", "vCenter Hypervisor CPU性能监测",
						"使用web service sdk监测HYPERVISOR CPU性能信息。", MultiInstanceConfiger.class.getName(),
						VCenterHypervisorCPUMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("HYPERVISOR", "HYPERVISOR-VCENTER-INFO", "vCenter Hypervisor 基本信息监测 [可用性]",
						"使用web service sdk监测HYPERVISOR 基本信息。", MultiInstanceConfiger.class.getName(),
						VCenterHypervisorInfoMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("HYPERVISOR", "HYPERVISOR-VCENTER-RAM", "vCenter Hypervisor 内存性能监测",
						"使用web service sdk监测HYPERVISOR 内存性能信息。", MultiInstanceConfiger.class.getName(),
						VCenterHypervisorRAMMonitor.class.getName(), index++, targetTypeIds, methodTypeIds)};
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("HYPERVISOR-VCENTER-CPU", "HYPERVISOR-VCENTER-CPU-1", "主机IP", "", "esx主机IP", MonitorItemType.TEXT),
				new MonitorItem("HYPERVISOR-VCENTER-CPU", "HYPERVISOR-VCENTER-CPU-2", "利用率", "%", "esx主机CPU利用率", MonitorItemType.NUMBER),
				new MonitorItem("HYPERVISOR-VCENTER-CPU", "HYPERVISOR-VCENTER-CPU-3", "使用量", "MHz", "esx主机CPU使用量", MonitorItemType.NUMBER),

				new MonitorItem("HYPERVISOR-VCENTER-INFO","HYPERVISOR-VCENTER-INFO-1", "主机IP", "", "主机IP", MonitorItemType.TEXT),
				new MonitorItem("HYPERVISOR-VCENTER-INFO","HYPERVISOR-VCENTER-INFO-2", "产品", "", "esx主机软件产品名称", MonitorItemType.TEXT),
				new MonitorItem("HYPERVISOR-VCENTER-INFO","HYPERVISOR-VCENTER-INFO-3", "CPU个数", "个", "esx主机CPU个数", MonitorItemType.NUMBER),
				new MonitorItem("HYPERVISOR-VCENTER-INFO","HYPERVISOR-VCENTER-INFO-4", "CPU总容量", "MHz", "esx主机CPU总容量", MonitorItemType.NUMBER),
				new MonitorItem("HYPERVISOR-VCENTER-INFO","HYPERVISOR-VCENTER-INFO-5", "CPU预留量", "MHz", "esx主机CPU预留量", MonitorItemType.NUMBER),
				new MonitorItem("HYPERVISOR-VCENTER-INFO","HYPERVISOR-VCENTER-INFO-6", "内存总容量", "MB", "esx主机内存总容量", MonitorItemType.NUMBER),
				new MonitorItem("HYPERVISOR-VCENTER-INFO","HYPERVISOR-VCENTER-INFO-7", "内存授权量", "MB", "esx主机内存授权量", MonitorItemType.NUMBER),
				new MonitorItem("HYPERVISOR-VCENTER-INFO","HYPERVISOR-VCENTER-INFO-8", "内存预留量", "MB", "esx主机内存预留量", MonitorItemType.NUMBER),
				new MonitorItem("HYPERVISOR-VCENTER-INFO","HYPERVISOR-VCENTER-INFO-9", "网卡个数", "个", "esx主机网卡个数", MonitorItemType.NUMBER),
				new MonitorItem("HYPERVISOR-VCENTER-INFO","HYPERVISOR-VCENTER-INFO-10", "存储总容量", "MB", "esx主机存储总容量", MonitorItemType.NUMBER),
				new MonitorItem("HYPERVISOR-VCENTER-INFO","HYPERVISOR-VCENTER-INFO-11", "运行状态", "", "esx主机运行状态", MonitorItemType.TEXT),
				new MonitorItem("HYPERVISOR-VCENTER-INFO","HYPERVISOR-VCENTER-INFO-12", "运行虚拟机数", "个", "esx主机中运行的虚拟机个数", MonitorItemType.NUMBER),
				new MonitorItem("HYPERVISOR-VCENTER-INFO","HYPERVISOR-VCENTER-INFO-13", "所在集群", "", "所在集群", MonitorItemType.TEXT),
				new MonitorItem("HYPERVISOR-VCENTER-INFO","HYPERVISOR-VCENTER-INFO-14", "虚拟数据中心", "", "虚拟数据中心", MonitorItemType.TEXT),

				new MonitorItem("HYPERVISOR-VCENTER-RAM", "HYPERVISOR-VCENTER-RAM-1", "主机IP", "", "esx主机IP", MonitorItemType.TEXT),
				new MonitorItem("HYPERVISOR-VCENTER-RAM", "HYPERVISOR-VCENTER-RAM-2", "利用率", "%", "esx主机内存利用率", MonitorItemType.NUMBER),
				new MonitorItem("HYPERVISOR-VCENTER-RAM", "HYPERVISOR-VCENTER-RAM-3", "使用量", "MB", "esx主机内存使用量", MonitorItemType.NUMBER),
				new MonitorItem("HYPERVISOR-VCENTER-RAM", "HYPERVISOR-VCENTER-RAM-4", "虚拟机内存使用量", "MB", "esx主机中所有虚拟机的内存使用量",
						MonitorItemType.NUMBER) };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(VSphereMonitorMethodOption.TYPE_ID, "VSphere监测协议",
				VSphereConfPanel.class), };
	}
}
