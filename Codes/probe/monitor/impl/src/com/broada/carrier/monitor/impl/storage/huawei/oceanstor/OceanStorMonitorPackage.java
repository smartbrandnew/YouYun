package com.broada.carrier.monitor.impl.storage.huawei.oceanstor;

import com.broada.carrier.monitor.impl.storage.huawei.oceanstor.bandwidth.BandwidthMonitor;
import com.broada.carrier.monitor.impl.storage.huawei.oceanstor.cpu.CPUMonitor;
import com.broada.carrier.monitor.impl.storage.huawei.oceanstor.info.HostInfoMonitor;
import com.broada.carrier.monitor.impl.storage.huawei.oceanstor.io.IOMonitor;
import com.broada.carrier.monitor.impl.storage.huawei.oceanstor.iops.IOPSMonitor;
import com.broada.carrier.monitor.impl.storage.huawei.oceanstor.lun.LUNMonitor;
import com.broada.carrier.monitor.impl.storage.huawei.oceanstor.memory.MemoryMonitor;
import com.broada.carrier.monitor.impl.storage.huawei.oceanstor.partition.PartitionMonitor;
import com.broada.carrier.monitor.impl.storage.huawei.oceanstor.raid.RAIDMonitor;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class OceanStorMonitorPackage implements MonitorPackage{
	
	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "OceanStor" };
		String[] methodTypeIds = new String[] {  SnmpMethod.TYPE_ID};
		int index = 1;
		return new MonitorType[]{
				new MonitorType("OCEANSTOR", "OCEANSTOR-CPU", "OceanStorCPU利用率", 
						"通过SNMP方式采集OCEANSTOR-CPU利用率", null, CPUMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				/*new MonitorType("OCEANSTOR", "OCEANSTOR-RAM", "OceanStor内存使用率", 
						"通过SNMP方式采集OCEANSTOR内存使用率", null, RamMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),*/
				new MonitorType("OCEANSTOR", "OCEANSTOR-IO", "OceanStorIO使用率", 
						"通过SNMP方式采集OCEANSTOR-IO使用率", null, IOMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				/*new MonitorType("OCEANSTOR", "OCEANSTOR-RATE", "OceanStor传输速率", 
						"通过SNMP方式采集OCEANSTOR传输速率", null, TransmissionRateMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),*/
				new MonitorType("OCEANSTOR", "OCEANSTOR-PARTITION", "OceanStor分区数", 
								"通过SNMP方式采集OCEANSTOR分区数", null, PartitionMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				new MonitorType("OCEANSTOR", "OCEANSTOR-BANDWIDTH", "OceanStor带宽", 
						"通过SNMP方式采集OCEANSTOR带宽", null, BandwidthMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				new MonitorType("OCEANSTOR", "OCEANSTOR-IOPS", "OceanStor吞吐量", 
								"通过SNMP方式采集OCEANSTOR吞吐量", null, IOPSMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				/*new MonitorType("OCEANSTOR", "OCEANSTOR-RESPONSE-TIME", "OceanStor响应时间", 
						"通过SNMP方式采集OCEANSTOR响应时间", null, ResponseTimeMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),*/
				new MonitorType("OCEANSTOR", "OCEANSTOR-RAID", "OceanStor存储池信息", 
								"通过SNMP方式采集OCEANSTOR存储池信息", null, RAIDMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				new MonitorType("OCEANSTOR", "OCEANSTOR-LUN", "OceanStor逻辑卷信息", 
						"通过SNMP方式采集OCEANSTOR逻辑卷信息", null, LUNMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				/*new MonitorType("OCEANSTOR", "OCEANSTOR-HOST", "OceanStor连接主机信息", 
						"通过SNMP方式采集OCEANSTOR连接主机信息", null, HostInfoMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),*/
				new MonitorType("OCEANSTOR", "OCEANSTOR-MEMORY", "OceanStor存储容量信息", 
						"通过SNMP方式采集OCEANSTOR存储容量信息", null, MemoryMonitor.class.getName(), index++, targetTypeIds, methodTypeIds)
		};
	}
	
	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("OCEANSTOR-CPU", "OCEANSTOR-CPU-1", "CPU利用率", "%", "CPU利用率", MonitorItemType.NUMBER),
				/*new MonitorItem("OCEANSTOR-RAM", "OCEANSTOR-RAM-1", "内存利用率", "%", "内存利用率", MonitorItemType.NUMBER),*/
				/*new MonitorItem("OCEANSTOR-RATE", "OCEANSTOR-RATE-1", "传输速率", "MB/s", "传输速率利用率", MonitorItemType.NUMBER),*/
				new MonitorItem("OCEANSTOR-IO", "OCEANSTOR-IO-1", "IO利用率", "IO/s", "IO利用率", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR-PARTITION", "OCEANSTOR-PARTITION-1", "分区数", "个", "设备分区数", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR-BANDWIDTH", "OCEANSTOR-BANDWIDTH-1", "带宽", "MB/s", "设备带宽", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR-IOPS", "OCEANSTOR-IOPS-1", "吞吐量", "IO/s", "吞吐量", MonitorItemType.NUMBER),
				/*new MonitorItem("OCEANSTOR-RESPONSE-TIME", "OCEANSTOR-RESPONSE-TIME-1", "响应时间", "us", "响应时间", MonitorItemType.NUMBER),*/
				
				// 存储池信息
				new MonitorItem("OCEANSTOR-RAID", "OCEANSTOR-RAID-1", "存储池总容量", "MB", "存储池总容量", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR-RAID", "OCEANSTOR-RAID-2", "存储池已用容量", "MB", "存储池已用容量", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR-RAID", "OCEANSTOR-RAID-3", "存储池可用容量", "MB", "存储池可用容量", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR-RAID", "OCEANSTOR-RAID-4", "存储池数据保护容量", "MB", "存储池数据保护容量", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR-RAID", "OCEANSTOR-RAID-5", "存储池Tier0裸容量", "MB", "存储池Tier0裸容量", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR-RAID", "OCEANSTOR-RAID-6", "存储池Tier1裸容量", "MB", "存储池Tier1裸容量", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR-RAID", "OCEANSTOR-RAID-7", "存储池Tier2裸容量", "MB", "存储池Tier2裸容量", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR-RAID", "OCEANSTOR-RAID-8", "存储池已用容量阈值", "%", "存储池已用容量阈值", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR-RAID", "OCEANSTOR-RAID-9", "存储池迁移粒度", "KB", "存储池迁移粒度", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR-RAID", "OCEANSTOR-RAID-10", "存储池待上迁数据量", "MB", "存储池待上迁数据量", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR-RAID", "OCEANSTOR-RAID-11", "存储池待下迁数据量", "MB", "存储池待下迁数据量", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR-RAID", "OCEANSTOR-RAID-12", "存储池迁移时间", "s", "存储池迁移时间", MonitorItemType.NUMBER),
				
				// 逻辑卷信息
				new MonitorItem("OCEANSTOR-LUN", "OCEANSTOR-LUN-1", "逻辑卷的容量", "KB", "逻辑卷的容量", MonitorItemType.NUMBER),
				
				// 连接主机信息
				/*new MonitorItem("OCEANSTOR-HOST", "OCEANSTOR-HOST-1", "主机名称", "", "主机名称", MonitorItemType.TEXT),
				new MonitorItem("OCEANSTOR-HOST", "OCEANSTOR-HOST-2", "主机位置", "", "主机位置", MonitorItemType.TEXT),
				new MonitorItem("OCEANSTOR-HOST", "OCEANSTOR-HOST-3", "主机操作系统", "", "主机操作系统", MonitorItemType.TEXT),
				new MonitorItem("OCEANSTOR-HOST", "OCEANSTOR-HOST-4", "主机IP地址", "", "主机IP地址", MonitorItemType.TEXT),
				new MonitorItem("OCEANSTOR-HOST", "OCEANSTOR-HOST-5", "主机域名", "", "主机域名", MonitorItemType.TEXT),
				new MonitorItem("OCEANSTOR-HOST", "OCEANSTOR-HOST-6", "主机型号", "", "主机型号", MonitorItemType.TEXT),*/
				
				// 存储容量信息
				new MonitorItem("OCEANSTOR-MEMORY", "OCEANSTOR-MEMORY-1", "存储设备总容量", "MB", "存储设备总容量", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR-MEMORY", "OCEANSTOR-MEMORY-2", "存储设备已使用容量", "MB", "存储设备已使用容量", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR-MEMORY", "OCEANSTOR-MEMORY-3", "存储设备容量使用率", "%", "存储设备容量使用率", MonitorItemType.NUMBER)
		};
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return null;
	}
}
