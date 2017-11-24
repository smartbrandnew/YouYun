package com.broada.carrier.monitor.impl.virtual.hypervisor;

import com.broada.carrier.monitor.impl.virtual.hypervisor.cpu.CLIHyperVCPUConfiger;
import com.broada.carrier.monitor.impl.virtual.hypervisor.cpu.CLIHyperVCPUMonitor;
import com.broada.carrier.monitor.impl.virtual.hypervisor.info.CLIHyperVInfoConfiger;
import com.broada.carrier.monitor.impl.virtual.hypervisor.info.CLIHyperVInfoMontior;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class HypervisorMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		return new MonitorType[] {
				new MonitorType("HYPERVISOR", "hyperVInfo", "HyperV虚拟机信息监测(CLI)", "通过WMI进行hyperV虚拟机状态监测",
						CLIHyperVInfoConfiger.class.getName(), CLIHyperVInfoMontior.class.getName(), 1,
						new String[] { "Hypervisor" }, new String[] { CLIMonitorMethodOption.TYPE_ID }),
				new MonitorType("HYPERVISOR", "hyperVCPU", "HyperV虚拟机CPU监测(CLI)", "通过WMI进行hyperV虚拟机CPU监测",
						CLIHyperVCPUConfiger.class.getName(), CLIHyperVCPUMonitor.class.getName(), 2,
						new String[] { "Hypervisor" }, new String[] { CLIMonitorMethodOption.TYPE_ID }) };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] { new MonitorItem("hyperVInfo","CLI-hyperVState-1", "当前状态", null, "当前状态", MonitorItemType.TEXT),
				new MonitorItem("hyperVInfo","CLI-hyperVState-2", "健康状态", null, "健康状态", MonitorItemType.TEXT),
				new MonitorItem("hyperVInfo","CLI-hyperVState-3", "运行时间", null, "运行时间", MonitorItemType.TEXT),
				new MonitorItem("hyperVInfo","CLI-hyperVState-4", "安装时间", null, "安装时间", MonitorItemType.TEXT),
				new MonitorItem("hyperVInfo","CLI-hyperVState-5", "操作系统", null, "操作系统", MonitorItemType.TEXT),
				new MonitorItem("hyperVInfo","CLI-hyperVState-6", "分配内存(M)", null, "分配内存(M)", MonitorItemType.TEXT),
				new MonitorItem("hyperVInfo","CLI-hyperVState-7", "CPU核心数", null, "CPU核心数", MonitorItemType.TEXT),
				new MonitorItem("hyperVInfo","CLI-hyperVState-8", "最后状态改变时间", null, "最后状态改变时间", MonitorItemType.TEXT),
				new MonitorItem("hyperVInfo","CLI-hyperVState-9", "备注", null, "备注", MonitorItemType.TEXT),

				new MonitorItem("hyperVCPU", "CLI-hyperVCPU-2", "类型", null, "类型", MonitorItemType.TEXT),
				new MonitorItem("hyperVCPU", "CLI-hyperVCPU-3", "CPU使用率(%)", null, "CPU使用率(%)", MonitorItemType.NUMBER) };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return null;
	}
}
