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

public class HP3PARMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "DiskArrayHP" };
		String[] methodTypeIds = new String[] { SmisMethod.TYPE_ID, SnmpMethod.TYPE_ID, CLIMonitorMethodOption.TYPE_ID };
		int index = 1;

		String monitorClz = GenericMonitor.class.getName();
		// String monitorClz = StorageBaseMonitor.class.getName();
		String multiInstanceClz = GenericConfiger.class.getName();
		// String multiInstanceClz = StorageConfiger.class.getName();

		return new MonitorType[] {

				new MonitorType("HP_3PAR", "HP3PAR-STORAGESYSTEM-INFO", "HP3Par存储系统信息监测", "监测存储系统基本信息使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("HP_3PAR", "HP3PAR-POWERSUPPLY-INFO", "HP3Par电源基本信息监测", "监测阵列电源模块基本信息使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("HP_3PAR", "HP3PAR-DISKDRIVER-INFO", "HP3Par磁盘信息基本信息监测", "监测磁盘信息基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("HP_3PAR", "HP3PAR-STORAGEPOOL-INFO", "HP3Par存储池基本信息监测", "监测存储池基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("HP_3PAR", "HP3PAR-STORAGEVOLUME-INFO", "HP3Par存储卷基本信息监测", "监测存储卷基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("HP_3PAR", "HP3PAR-FAN-INFO", "HP3Par风扇基本信息监测", "监测风扇基本信息情况。", multiInstanceClz,
						monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("HP_3PAR", "HP3PAR-STORAGECONTROLLER-INFO", "HP3Par控制器基本信息监测", "监测控制器基本信息情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("HP_3PAR", "HP3PAR-FCPORT-INFO", "HP3Par光纤端口基本信息监测", "监测光纤端口基本信息情况。", multiInstanceClz,
						monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("HP_3PAR", "HP3PAR-BETTERY-INFO", "HP3Par电池基本信息监测", "监测电池基本信息情况。", multiInstanceClz,
						monitorClz, index++, targetTypeIds, methodTypeIds)

		};
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
