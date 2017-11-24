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

public class EMCCelerraMonitorPackage implements MonitorPackage{

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "EMC"};
		String[] methodTypeIds = new String[] { SmisMethod.TYPE_ID, SnmpMethod.TYPE_ID, CLIMonitorMethodOption.TYPE_ID};
		int index = 1;

		String monitorClz = StorageBaseMonitor.class.getName();
		String multiInstanceClz = StorageConfiger.class.getName();

		return new MonitorType[] {
				new MonitorType("EMC_CELERRA", "EMCCELERRA-ARRAY-INFO", "EMCCelerra阵列信息监测", "监测阵列基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("EMC_CELERRA", "EMCCELERRA-DISKDRIVER-INFO", "EMCCelerra物理磁盘基本信息监测", "监测阵列物理磁盘基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),

				new MonitorType("EMC_CELERRA", "EMCCELERRA-CIFSSHARE-INFO", "EMCCelerraCIFS共享基本信息监测", "监测阵列CIFS共享基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("EMC_CELERRA", "EMCCELERRA-CIFSSERVER-INFO", "EMCCelerraCIFS服务基本信息监测", "监测阵列CIFS服务基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
								
				new MonitorType("EMC_CELERRA", "EMCCELERRA-NFSSERVER-INFO", "EMCCelerraNFS服务基本信息监测", "监测阵列NFS服务本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
						
				new MonitorType("EMC_CELERRA", "EMCCELERRA-NFSSHARE-INFO", "EMCCelerraNFS共享基本信息监测", "监测阵列NFS共享基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("EMC_CELERRA", "EMCCELERRA-STORAGEPOOL-INFO", "EMCCelerra存储池基本信息监测", "监测阵列存储池基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("EMC_CELERRA", "EMCCELERRA-STORAGEEXTENT-INFO", "EMCCelerra存储信息基本信息监测", "监测阵列存储信息基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("EMC_CELERRA", "EMCCELERRA-COMPUTERSYSTEM-INFO", "EMCCelerra电脑系统基本信息监测", "监测电脑系统上光纤端口基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("EMC_CELERRA", "EMCCELERRA-FILESYSTEM-INFO", "EMCCelerra文件系统基本信息监测", "监测阵列文件系统基本信息及容量使用情况。",
						multiInstanceClz, monitorClz, index++, targetTypeIds, methodTypeIds),
				
				new MonitorType("EMC_CELERRA", "EMCCELERRA-NETWORKPORT-INFO", "EMCCelerra网络端口基本信息监测", "监测控制器网络端口基本信息及容量使用情况。",
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
