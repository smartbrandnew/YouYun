package com.broada.carrier.monitor.impl.mw.apache;

import com.broada.carrier.monitor.method.apache.ApacheMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class ApacheMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] apacheOption = new String[] { ApacheMethodOption.TYPE_ID };
		int index = 1;
		return new MonitorType[] { new MonitorType("APACHE", "APACHE", "APACHE性能 [可用性]", "监测APACHE服务的性能状态。",
				ApacheParamConfiger.class.getName(), ApacheMonitor.class.getName(), index++, new String[] { "Apache" },
				apacheOption) };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] { new MonitorItem("APACHE","APACHE-1", "累计访问数", "个", "累计全部的访问计数", MonitorItemType.NUMBER),
				new MonitorItem("APACHE","APACHE-2", "累计数据处理量", "KB", "累计全部的数据处理量", MonitorItemType.NUMBER),
				new MonitorItem("APACHE","APACHE-3", "CPU负载", "%", "CPU的负载", MonitorItemType.NUMBER),
				new MonitorItem("APACHE","APACHE-4", "正常运行时间", "秒", "到此刻止累计正常运行时间", MonitorItemType.NUMBER),
				new MonitorItem("APACHE","APACHE-5", "每秒请求", "个", "平均每秒的请求数", MonitorItemType.NUMBER),
				new MonitorItem("APACHE","APACHE-6", "每秒处理字节", "Byte", "平均每秒处理的字节数", MonitorItemType.NUMBER),
				new MonitorItem("APACHE","APACHE-7", "每请求处理字节", "Byte", "平均每个请求处理的字节数", MonitorItemType.NUMBER),
				new MonitorItem("APACHE","APACHE-8", "忙作业", "个", "正在处理的作业数", MonitorItemType.NUMBER),
				new MonitorItem("APACHE","APACHE-9", "空闲作业", "个", "空闲的作业数", MonitorItemType.NUMBER),
				new MonitorItem("APACHE","APACHE-10", "响应时间", "秒", "向APACHE服务器发出本次请求的响应时间", MonitorItemType.NUMBER) };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		// TODO Auto-generated method stub
		return null;
	}

}
