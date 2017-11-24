package com.broada.carrier.monitor.impl.stdsvc.ldap;

import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.mw.tuxedo.machine.TuxMheMonitor;
import com.broada.carrier.monitor.method.ldap.LdapMethod;
import com.broada.carrier.monitor.method.ldap.LdapMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

/**
 * Created by hu on 2015/5/18.
 */
public class LdapMonitorPackage implements MonitorPackage {
	@Override public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "LDAP" };
		String[] methodTypeIds = new String[] { LdapMethod.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("LDAP","LDAP", "LDAP监测", "使用LDAP协议监测指定的目录和名字服务运行是否正常。", SingleInstanceConfiger.class.getName(),
						TuxMheMonitor.class.getName(), index++, targetTypeIds, methodTypeIds) };
	}

	@Override public MonitorItem[] getItems() {
		return new MonitorItem[] {};
	}

	@Override public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] {
				new MonitorMethodType(LdapMethod.TYPE_ID, "LDAP 监测协议", LdapMethodConfiger.class) };
	}
}
