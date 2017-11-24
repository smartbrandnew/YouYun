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

public class NETAPPFASSMISMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "DiskArrayNETAPP" };
		String[] methodTypeIds = new String[] { SmisMethod.TYPE_ID,
				SnmpMethod.TYPE_ID, CLIMonitorMethodOption.TYPE_ID };
		int index = 1;

		String monitorClz = GenericMonitor.class.getName();
		// String monitorClz = StorageBaseMonitor.class.getName();
		String multiInstanceClz = GenericConfiger.class.getName();
		// String multiInstanceClz = StorageConfiger.class.getName();

		return new MonitorType[] {

				new MonitorType("NETAPP_FAS_SMIS","NETAPPFAS-STORAGESYSTEM-INFO",
						"NETAPPFAS存储系统基本信息监测(SMIS)", "监测存储系统基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds,
						methodTypeIds),

				new MonitorType("NETAPP_FAS_SMIS","NETAPPFAS-LUN-INFO",
						"NETAPPFAS LUN基本信息监测(SMIS)", "监测阵列LUN基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds,
						methodTypeIds),

				new MonitorType("NETAPP_FAS_SMIS","NETAPPFAS-STORAGEVOLUME-INFO",
						"NETAPPFAS存储卷基本信息监测(SMIS)", "监测阵列存储卷基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds,
						methodTypeIds) };
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
