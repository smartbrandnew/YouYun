package com.broada.carrier.monitor.impl.storage;

import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.smis.SmisMethod;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class IBMDSMonitorPackage implements MonitorPackage{

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "DiskArrayIBM"};
		String[] methodTypeIds = new String[] { SmisMethod.TYPE_ID, SnmpMethod.TYPE_ID, CLIMonitorMethodOption.TYPE_ID};
		int index = 1;

		String monitorClz = StorageBaseMonitor.class.getName();
		String multiInstanceClz = StorageConfiger.class.getName();

		return new MonitorType[] {
				
				new MonitorType("IBM_DS", "IBMDS-DISKDRIVER-INFO", "IBMDS磁盘基本信息监测", "监测磁盘基本信息。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
						
				new MonitorType("IBM_DS", "IBMDS-DISKDRIVER-IO", "IBMDS磁盘性能信息监测", "监测磁盘性能信息。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
						
				new MonitorType("IBM_DS", "IBMDS-STORAGESYSTEM-INFO", "IBMDS存储系统基本信息监测", "监测存储系统基本信息。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
						
				new MonitorType("IBM_DS", "IBMDS-POWERSUPPLY-INFO", "IBMDS电源基本信息监测", "监测电源基本信息。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
						
				new MonitorType("IBM_DS", "IBMDS-FAN-INFO", "IBMDS风扇基本信息监测", "监测风扇基本信息。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
						
				new MonitorType("IBM_DS", "IBMDS-STORAGEVOLUME-INFO", "IBMDS存储卷基本信息监测", "监测存储卷基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
								
				new MonitorType("IBM_DS", "IBMDS-FCPORT-INFO", "IBMDS光纤端口基本信息监测", "监测光纤端口基本信息。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
						
				new MonitorType("IBM_DS", "IBMDS-STORAGEPOOL-INFO", "IBMDS存储池基本信息监测", "监测存储池基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
						
				new MonitorType("IBM_DS", "IBMDS-CONTROLLER-INFO", "IBMDS控制器基本信息监测", "监测控制器基本信息。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
								
				new MonitorType("IBM_DS", "IBMDS-BATTERY-INFO", "IBMDS电池基本信息监测", "监测电池基本信息。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds)
						
						//new MonitorType("IBMDS-STORAGESTATISTIC-INFO", "IBMDS存储统计基本信息监测", "监测存储统计基本信息及容量使用情况。",
						//		multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
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
