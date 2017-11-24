package com.broada.carrier.monitor.impl.mw.tomcat;

import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.mw.tomcat.basic.TomcatBasicMonitor;
import com.broada.carrier.monitor.impl.mw.tomcat.jvm.TomcatJVMMonitor;
import com.broada.carrier.monitor.method.tomcat.TomcatMonitorMethodOption;
import com.broada.carrier.monitor.method.tomcat.TomcatParamPanel;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

/**
 * Created by hu on 2015/5/18.
 */
public class TomcatMonitorPackage implements MonitorPackage {
	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "Tomcat" };
		String[] methodTypeIds = new String[] { TomcatMonitorMethodOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("TOMCAT", "TOMCAT-BASIC", "Tomcat基本信息采集 [可用性]", "采集Tomcat服务器的基本信息,并监测其工作状态是否正常。",
						SingleInstanceConfiger.class.getName(), TomcatBasicMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				new MonitorType("TOMCAT", "TOMCAT-JVM", "Tomcat JVM内存信息采集", "监测Tomcat的JVM内存使用情况。",
						SingleInstanceConfiger.class.getName(), TomcatJVMMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds) };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("TOMCAT-BASIC", "TOMCAT-BASIC-1", "Tomcat版本", "", "Tomcat版本", MonitorItemType.TEXT),
				new MonitorItem("TOMCAT-BASIC", "TOMCAT-BASIC-2", "JVM版本", "", "JVM版本", MonitorItemType.TEXT),
				new MonitorItem("TOMCAT-BASIC", "TOMCAT-BASIC-3", "JVM厂商", "", "JVM厂商", MonitorItemType.TEXT),
				new MonitorItem("TOMCAT-BASIC", "TOMCAT-BASIC-4", "操作系统", "", "操作系统", MonitorItemType.TEXT),
				new MonitorItem("TOMCAT-BASIC", "TOMCAT-BASIC-5", "操作系统版本", "", "操作系统版本", MonitorItemType.TEXT),
				new MonitorItem("TOMCAT-BASIC", "TOMCAT-BASIC-6", "系统结构", "", "系统结构", MonitorItemType.TEXT),
				new MonitorItem("TOMCAT-BASIC", "TOMCAT-BASIC-7", "响应时间", "毫秒", "链接Tomcat服务器的响应时间",
						MonitorItemType.NUMBER),
				new MonitorItem("TOMCAT-JVM", "TOMCAT-JVM-1", "JVM可用内存", "MB", "JVM可用内存", MonitorItemType.NUMBER),
				new MonitorItem("TOMCAT-JVM", "TOMCAT-JVM-2", "JVM内存总数", "MB", "JVM内存总数", MonitorItemType.NUMBER),
				new MonitorItem("TOMCAT-JVM", "TOMCAT-JVM-3", "JVM已用内存", "MB", "JVM已用内存", MonitorItemType.NUMBER) };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(TomcatMonitorMethodOption.TYPE_ID, "Tomcat Agent监测协议",
				TomcatParamPanel.class) };
	}
}
