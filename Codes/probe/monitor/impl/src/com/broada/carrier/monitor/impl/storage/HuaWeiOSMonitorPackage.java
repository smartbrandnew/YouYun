package com.broada.carrier.monitor.impl.storage;

import com.broada.carrier.monitor.impl.generic.GenericConfiger;
import com.broada.carrier.monitor.impl.generic.GenericMonitor;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.script.ScriptMethod;
import com.broada.carrier.monitor.method.smis.SmisMethod;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class HuaWeiOSMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "DiskArrayHuawei"};
		String[] methodTypeIds = new String[] { ScriptMethod.TYPE_ID,SmisMethod.TYPE_ID, SnmpMethod.TYPE_ID, CLIMonitorMethodOption.TYPE_ID};
		int index = 1;

		String monitorClz = GenericMonitor.class.getName();
		//String monitorClz = StorageBaseMonitor.class.getName();
		String multiInstanceClz = GenericConfiger.class.getName();
		//String multiInstanceClz = StorageConfiger.class.getName();

		return new MonitorType[] {
				new MonitorType("HUAWEI_DEVICE", "HUAWEI-STORAGESYSTEM-INFO", "HuaWei存储系统基本信息监测", "监测存储系统基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("HUAWEI_DEVICE", "HUAWEI-DISKDRIVER-INFO", "HuaWei物理磁盘基本信息监测", "监测阵列物理磁盘基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("HUAWEI_DEVICE", "HUAWEI-STORAGEVOLUME-INFO", "HuaWei存储卷基本信息监测", "监测阵列存储卷基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("HUAWEI_DEVICE", "HUAWEI-STORAGEPOOL-INFO", "HuaWei存储池基本信息监测", "监测阵列存储池基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("HUAWEI_DEVICE", "HUAWEI-STORAGECONTROLLER-INFO", "HuaWei控制器基本信息监测", "监测阵列控制器基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("HUAWEI_DEVICE", "HUAWEI-FCPORT-INFO", "HuaWei光纤端口基本信息监测", "监测控制器上光纤端口基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("HUAWEI_DEVICE", "HUAWEI-NETWORKPORT-INFO", "HuaWei网络端口基本信息监测", "监测控制器网络端口基本信息情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds)/*,
				华为统一存储smis尚未实现。
				new MonitorType("HW-BATTERY-INFO", "HuaWei电池监测", "监测阵列电池信息。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("HW-POWERSUPPLY-INFO", "HuaWei电源监测", "监测阵列电源信息。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("HW-FAN-INFO", "HuaWei风扇系统监测", "监测阵列风扇信息。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds)*/
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
