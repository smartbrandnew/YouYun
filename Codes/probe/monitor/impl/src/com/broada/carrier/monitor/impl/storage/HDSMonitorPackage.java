package com.broada.carrier.monitor.impl.storage;

import com.broada.carrier.monitor.impl.generic.GenericConfiger;
import com.broada.carrier.monitor.impl.generic.GenericMonitor;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.smis.SmisMethod;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class HDSMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "DiskArrayHDS" };
		String[] methodTypeIds = new String[] { SmisMethod.TYPE_ID,
				SnmpMethod.TYPE_ID, CLIMonitorMethodOption.TYPE_ID };
		int index = 1;

		String monitorClz = GenericMonitor.class.getName();
		// String monitorClz = StorageBaseMonitor.class.getName();
		String multiInstanceClz = GenericConfiger.class.getName();
		// String multiInstanceClz = StorageConfiger.class.getName();

		return new MonitorType[] {

				new MonitorType("HDS", "HDS-STORAGEVOLUME-INFO", "HDS存储卷基本信息监测",
						"监测存储卷基本信息及容量使用情况。", multiInstanceClz, monitorClz,
						index++, targetTypeIds, methodTypeIds),

				new MonitorType("HDS", "HDS-STORAGEPOOL-INFO", "HDS存储池基本信息监测",
						"监测存储池基本信息及容量使用情况。", multiInstanceClz, monitorClz,
						index++, targetTypeIds, methodTypeIds),

				new MonitorType("HDS", "HDS-STORAGESYSTEM-INFO", "HDS存储系统基本信息监测",
						"监测存储系统基本信息及容量使用情况。", multiInstanceClz, monitorClz,
						index++, targetTypeIds, methodTypeIds),

				new MonitorType("HDS", "HDS-BATTERY-INFO", "HDS电池基本信息监测",
						"监测电池基本信息及容量使用情况。", multiInstanceClz, monitorClz,
						index++, targetTypeIds, methodTypeIds),

				new MonitorType("HDS", "HDS-POWERSUPPLY-INFO", "HDS电源基本信息监测",
						"监测电源基本信息及容量使用情况。", multiInstanceClz, monitorClz,
						index++, targetTypeIds, methodTypeIds),

				new MonitorType("HDS", "HDS-FAN-INFO", "HDS风扇基本信息监测",
						"监测风扇基本信息及容量使用情况。", multiInstanceClz, monitorClz,
						index++, targetTypeIds, methodTypeIds),

				new MonitorType("HDS", "HDS-FCPORT-INFO", "HDS光纤端口基本信息监测",
						"监测光纤基本信息及容量使用情况。", multiInstanceClz, monitorClz,
						index++, targetTypeIds, methodTypeIds),

				new MonitorType("HDS", "HDS-DISKDRIVE-INFO", "HDS物理磁盘基本信息监测",
						"监测物理磁盘基本信息及容量使用情况。", multiInstanceClz, monitorClz,
						index++, targetTypeIds, methodTypeIds) };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {};
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] {};
	}
}
