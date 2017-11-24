package com.broada.carrier.monitor.impl.mw.tuxedo.machine;

import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

/**
 * Created by hu on 2015/5/18.
 */
public class TuxedoMonitorPackage implements MonitorPackage {
	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "Tuxedo" };
		String[] methodTypeIds = new String[] { SnmpMethod.TYPE_ID };
		int index = 1;

		return new MonitorType[] { new MonitorType("TUXEDO", "TUX-MACHINE", "Tuxedo Machine监测 [可用性]",
				"通过SNMP协议监测Tuxedo Machine的性能状态。", SingleInstanceConfiger.class.getName(),
				TuxMheMonitor.class.getName(), index++, targetTypeIds, methodTypeIds) };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("TUX-MACHINE", "TUX-MACHINE-1", "每秒处理队列服务数", "个", "机器每秒处理的队列服务数", MonitorItemType.NUMBER),
				new MonitorItem("TUX-MACHINE", "TUX-MACHINE-2", "每秒入队队列服务数", "个", "机器每秒入队的队列服务数", MonitorItemType.NUMBER),
				new MonitorItem("TUX-MACHINE", "TUX-MACHINE-3", "客户端数", "个", "当前客户端连接数", MonitorItemType.NUMBER),
				new MonitorItem("TUX-MACHINE", "TUX-MACHINE-4", "WS客户端数", "个", "当前WorkStation客户端数", MonitorItemType.NUMBER) };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return null;
	}
}
