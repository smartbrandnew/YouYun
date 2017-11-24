package com.broada.carrier.monitor.impl.storage.emc;

import com.broada.carrier.monitor.impl.storage.StorageBaseMonitor;
import com.broada.carrier.monitor.impl.storage.StorageConfiger;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.smis.SmisMethod;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class EMCSymmMonitorPackage implements MonitorPackage{

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "EMC"};
		String[] methodTypeIds = new String[] { SmisMethod.TYPE_ID, SnmpMethod.TYPE_ID, CLIMonitorMethodOption.TYPE_ID};
		int index = 1;

		String monitorClz = StorageBaseMonitor.class.getName();
		String multiInstanceClz = StorageConfiger.class.getName();

		return new MonitorType[] {
				new MonitorType("EMC_SYMM", "EMCSYMM-ARRAY-INFO", "EMCSymm阵列基本信息监测", "监测阵列基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("EMC_SYMM", "EMCSYMM-DISKDRIVER-INFO", "EMCSymm物理磁盘基本信息监测", "监测阵列物理磁盘基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
						
				new MonitorType("EMC_SYMM", "EMCSYMM-STORAGEVOLUME-INFO", "EMCSymm存储卷基本信息监测", "监测阵列存储卷基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("EMC_SYMM", "EMCSYMM-COMPUTERSYSTEM-INFO", "EMCSymm电脑系统基本信息监测", "监测电脑系统上光纤端口基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("EMC_SYMM", "EMCSYMM-FCPORT-INFO", "EMCSymm光纤端口基本信息监测", "监测阵列光纤端口基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds)
		};
	} 
	
	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {};
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] {
		};
	}
}
