package com.broada.carrier.monitor.impl.virtual.operationcenter;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.virtual.operationcenter.monitor.cloundvm.CloundVMMonitor;
import com.broada.carrier.monitor.impl.virtual.operationcenter.monitor.server.ServerMonitor;
import com.broada.carrier.monitor.method.operationcenter.OperationCenterMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class OperationCenterPackage implements MonitorPackage{

	@Override
	public MonitorItem[] getItems() {
		return null;
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(OperationCenterMethodOption.TYPE_ID, "OpeartionCenter监测协议",
				OperationCenterPackage.class)};
	}

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "Server" };
		String[] methodTypeIds = new String[] { OperationCenterMethodOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("OPERATIONCENTER", "OPERATIONCENTER-VM", "Operation Center 虚拟机性能监测",
						"Operation Center 虚拟机性能监测", MultiInstanceConfiger.class.getName(),
						CloundVMMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				new MonitorType("OPERATIONCENTER", "OPERATIONCENTER-SERVER", "Operation Center 主机性能监测",
						"Operation Center 主机性能监测", MultiInstanceConfiger.class.getName(),
						ServerMonitor.class.getName(), index++, targetTypeIds, methodTypeIds)};
	}

}
