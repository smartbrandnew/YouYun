package com.broada.carrier.monitor.impl.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.smis.SmisMethod;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.method.storage.StorageMethodConfig;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class IBMSVCMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "DiskArrayIBM" };
		String[] methodTypeIds = new String[] { SmisMethod.TYPE_ID, SnmpMethod.TYPE_ID, CLIMonitorMethodOption.TYPE_ID };
		int index = 1;

		String monitorClz = StorageBaseMonitor.class.getName();
		String multiInstanceClz = StorageConfiger.class.getName();

		return new MonitorType[] {

				new MonitorType("IBM_SVC", "IBMSVC-RAID-INFO", "IBMSVC RAID基本信息监测", "监测磁盘阵列基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("IBM_SVC", "IBMSVC-RAID-PERF", "IBMSVC RAID CPU内存监测", "监测磁盘阵列CPU,内存监测。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("IBM_SVC", "IBMSVC-RAID-CONTROLLER-INFO", "IBMSVC RAID控制器基本信息监测", "监测磁盘阵列控制器基本信息。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("IBM_SVC", "IBMSVC-RAID-CONTROLLER-PERF", "IBMSVC RAID控制器CPU内存及缓存监测",
						"监测磁盘阵列控制器CPU，内存，缓存使用情况。", multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("IBM_SVC", "IBMSVC-RAID-GROUP-INFO", "IBMSVC RAID组基本信息监测", "监测RAID组基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("IBM_SVC", "IBMSVC-STOREPOOL-INFO", "IBMSVC存储池基本信息监测", "监测存储池基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("IBM_SVC", "IBMSVC-STOREVOLUME-INFO", "IBMSVC存储卷基本信息监测", "监测存储卷基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("IBM_SVC", "IBMSVC-DISKDRIVER-INFO", "IBMSVC物理磁盘基本信息监测", "监测物理磁盘基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("IBM_SVC", "IBMSVC-DISKDRIVER-IO", "IBMSVC物理磁盘IO性能监测", "监测物理磁盘IO性能指标。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("IBM_SVC", "IBMSVC-NETDEVPORT-INFO", "IBMSVC以太网端口基本信息监测", "监测端口基本信息。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("IBM_SVC", "IBMSVC-LUN-INFO", "IBMSVCLUN基本信息监测", "监测LUN基本信息及容量监测。", multiInstanceClz,
						monitorClz, index++, targetTypeIds, methodTypeIds),
				new MonitorType("IBM_SVC", "IBMSVC-DISKARRAY-FCPORT-INFO", "IBMSVC光纤交换机端口基本信息监测", "监测端口基本信息。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				new MonitorType("IBM_SVC", "IBMSVC-DISKARRAY-FCPORT-IO", "IBMSVC端口IO监测", "监测端口IO使用情况,包括读写速率等。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				new MonitorType("IBM_SVC", "IBMSVC-DISKARRAY-POWERSUPPLY-INFO", "IBMSVC电源基本信息监测", "监测电源基本信息等。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				new MonitorType("IBM_SVC", "IBMSVC-DISKARRAY-FAN-INFO", "IBMSVC风扇基本信息监测", "监测风扇基本信息等。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds)
						/*
				new MonitorType("IBM_SVC", "IBMSVC-POWERSUPPLY-INFO", "IBMSVC电源基本信息监测", "监测电源基本信息等。", multiInstanceClz,
						monitorClz, index++, targetTypeIds, methodTypeIds)
						
				new MonitorType("IBM_SVC", "IBMSVC-FC-INFO", "IBMSVC光纤交换机基本信息监测", "监测光纤交换机基本信息。", multiInstanceClz,
						monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("IBM_SVC", "IBMSVC-FCPORT-INFO", "IBMSVC光纤交换机端口基本信息监测", "监测端口基本信息。", multiInstanceClz,
						monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("IBM_SVC", "IBMSVC-FCPORT-IO", "IBMSVC光纤交换机端口IO监测", "监测端口IO使用情况,包括读写速率等。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

		
		  new MonitorType("IBM_SVC","IBMSVC-FAN-INFO", "IBMSVC风扇基本信息监测",
		  "监测风扇基本信息等。", multiInstanceClz, monitorClz, index++, targetTypeIds,
		  methodTypeIds)
		 */
		};
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {};
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(SmisMethod.TYPE_ID, "SMI-S监测协议",
				StorageMethodConfig.class) };
	}
}
