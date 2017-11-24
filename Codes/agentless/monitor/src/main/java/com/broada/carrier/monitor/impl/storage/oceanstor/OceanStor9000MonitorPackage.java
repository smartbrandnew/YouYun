package com.broada.carrier.monitor.impl.storage.oceanstor;

import com.broada.carrier.monitor.impl.storage.oceanstor.cpu.CPUMonitor;
import com.broada.carrier.monitor.impl.storage.oceanstor.disk.DiskMonitor;
import com.broada.carrier.monitor.impl.storage.oceanstor.filesystem.FileSystemMonitor;
import com.broada.carrier.monitor.impl.storage.oceanstor.io.IOMonitor;
import com.broada.carrier.monitor.impl.storage.oceanstor.memory.MemoryMonitor;
import com.broada.carrier.monitor.impl.storage.oceanstor.quota.QuotaMonitor;
import com.broada.carrier.monitor.impl.storage.oceanstor.ram.RamMonitor;
import com.broada.carrier.monitor.impl.storage.oceanstor.transmission.TransmissionRateMonitor;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class OceanStor9000MonitorPackage implements MonitorPackage{
	
	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "OceanStor9000" };
		String[] methodTypeIds = new String[] {  SnmpMethod.TYPE_ID};
		int index = 1;
		return new MonitorType[]{
				new MonitorType("OCEANSTOR9000", "OCEANSTOR9000-CPU", "OceanStor9000CPU使用率", 
						"通过SNMP方式采集OCEANSTOR9000CPU使用率", null, CPUMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				new MonitorType("OCEANSTOR9000", "OCEANSTOR9000-RAM", "OceanStor9000内存使用率", 
						"通过SNMP方式采集OCEANSTOR9000内存使用率", null, RamMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				new MonitorType("OCEANSTOR9000", "OCEANSTOR9000-IO", "OceanStorIO9000IO使用率", 
						"通过SNMP方式采集OCEANSTOR9000IO使用率", null, IOMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				new MonitorType("OCEANSTOR9000", "OCEANSTOR9000-RATE", "OceanStor9000传输速率", 
						"通过SNMP方式采集OCEANSTOR9000传输速率", null, TransmissionRateMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				new MonitorType("OCEANSTOR9000", "OCEANSTOR9000-MEM", "OceanStor9000存储总量", 
						"通过SNMP方式采集OCEANSTOR9000存储总量", null, MemoryMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				new MonitorType("OCEANSTOR9000", "OCEANSTOR9000-DISK", "OceanStor9000磁盘信息采集", 
								"通过SNMP方式采集OCEANSTOR9000磁盘信息", null, DiskMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				new MonitorType("OCEANSTOR9000", "OCEANSTOR9000-FILESYSTEM", "OceanStor9000文件系统信息", 
										"通过SNMP方式采集OCEANSTOR9000文件系统信息", null, FileSystemMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				new MonitorType("OCEANSTOR9000", "OCEANSTOR9000-QUOTA", "OceanStor9000配额信息", 
						"通过SNMP方式采集OCEANSTOR9000配额信息", null, QuotaMonitor.class.getName(), index++, targetTypeIds, methodTypeIds)
		};
	}
	
	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("OCEANSTOR9000-CPU", "OCEANSTOR9000-CPU-1", "CPU利用率", "%", "CPU利用率", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR9000-RAM", "OCEANSTOR9000-RAM-1", "内存利用率", "%", "内存利用率", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR9000-IO", "OCEANSTOR9000-IO-1", "IO利用率", "%", "IO利用率", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR9000-RATE", "OCEANSTOR9000-RATE-1", "传输速率", "MB/s", "传输速率利用率", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR9000-MEM", "OCEANSTOR9000-MEM-1", "存储总量", "MB", "存储总量", MonitorItemType.NUMBER),
				new MonitorItem("OCEANSTOR9000-MEM", "OCEANSTOR9000-MEM-2", "存储利用率", "%", "存储利用率", MonitorItemType.NUMBER)};
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return null;
	}

}
