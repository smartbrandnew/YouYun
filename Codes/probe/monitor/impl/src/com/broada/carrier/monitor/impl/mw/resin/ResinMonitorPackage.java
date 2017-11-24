package com.broada.carrier.monitor.impl.mw.resin;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.mw.resin.baseInfo.ResinBaseInfoMonitor;
import com.broada.carrier.monitor.impl.mw.resin.connPool.ResinConnMonitor;
import com.broada.carrier.monitor.impl.mw.resin.ratio.ResinRatioMonitor;
import com.broada.carrier.monitor.impl.mw.resin.webApp.ResinWebAppMonitor;
import com.broada.carrier.monitor.method.resin.ResinJMXOption;
import com.broada.carrier.monitor.method.resin.ResinJMXPanel;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

/**
 * Created by hu on 2015/5/18.
 */
public class ResinMonitorPackage implements MonitorPackage {
	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "Resin" };
		String[] methodTypeIds = new String[] { ResinJMXOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("RESIN", "RESIN-BASEINFO", "Resin基本信息监测 [可用性]", "监测RESIN服务的基本信息。",
						SingleInstanceConfiger.class.getName(), ResinBaseInfoMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				new MonitorType("RESIN", "RESIN-CONNPOOL", "Resin连接池监测", "监测RESIN服务的连接池。",
						MultiInstanceConfiger.class.getName(), ResinConnMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				new MonitorType("RESIN", "RESIN-RATIO", "Resin命中率监测", "监测RESIN服务的监测代理缓存命中率，Block缓存命中率和调用命中率。",
						MultiInstanceConfiger.class.getName(), ResinRatioMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				new MonitorType("RESIN", "RESIN-WEBAPP", "ResinWEB应用监测", "监测RESIN服务的WEB应用。",
						MultiInstanceConfiger.class.getName(), ResinWebAppMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds) };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("RESIN-BASEINFO", "RESIN-BASEINFO-1", "服务序列号", "", "Resin服务的序列号", MonitorItemType.TEXT),
				new MonitorItem("RESIN-BASEINFO", "RESIN-BASEINFO-2", "服务版本", "", "Resin服务的版本", MonitorItemType.TEXT),
				new MonitorItem("RESIN-BASEINFO", "RESIN-BASEINFO-3", "服务配置文件路径", "", "Resin服务的配置文件路径", MonitorItemType.TEXT),
				new MonitorItem("RESIN-BASEINFO", "RESIN-BASEINFO-4", "ResinHome", "", "Resin服务的ResinHome", MonitorItemType.TEXT),
				new MonitorItem("RESIN-BASEINFO", "RESIN-BASEINFO-5", "RootDirectory", "", "Resin服务的RootDirectory", MonitorItemType.TEXT),
				new MonitorItem("RESIN-BASEINFO", "RESIN-BASEINFO-6", "主机名称", "", "运行Resin服务的主机名称", MonitorItemType.TEXT),
				new MonitorItem("RESIN-BASEINFO", "RESIN-BASEINFO-7", "当前状态", "", "Resin服务的当前状态", MonitorItemType.TEXT),
				new MonitorItem("RESIN-BASEINFO", "RESIN-BASEINFO-8", "运行时间", "", "Resin服务已经运行的时间", MonitorItemType.TEXT),
				new MonitorItem("RESIN-BASEINFO", "RESIN-BASEINFO-9", "总内存", "", "Resin服务所占的总内存", MonitorItemType.TEXT),
				new MonitorItem("RESIN-BASEINFO", "RESIN-BASEINFO-10", "剩余内存", "", "Resin服务所能使用的剩余内存", MonitorItemType.TEXT),
				new MonitorItem("RESIN-BASEINFO", "RESIN-BASEINFO-11", "当前CPU负载", "%", "Resin服务的当前CPU负载", MonitorItemType.NUMBER),
				new MonitorItem("RESIN-CONNPOOL","RESIN-CONNPOOL-1", "活跃连接数", "个", "正在使用的连接数", MonitorItemType.NUMBER),
				new MonitorItem("RESIN-CONNPOOL","RESIN-CONNPOOL-2", "空闲连接数", "个", "未使用的连接数", MonitorItemType.NUMBER),
				new MonitorItem("RESIN-CONNPOOL","RESIN-CONNPOOL-3", "连接命中率", "%", "连接命中比率", MonitorItemType.NUMBER),
				new MonitorItem("RESIN-CONNPOOL","RESIN-CONNPOOL-4", "已连接总数", "个", "已连接总数", MonitorItemType.NUMBER),
				new MonitorItem("RESIN-CONNPOOL","RESIN-CONNPOOL-5", "初始连接总数", "个", "已初始化产生的连接总数", MonitorItemType.NUMBER),
				new MonitorItem("RESIN-CONNPOOL","RESIN-CONNPOOL-6", "失败连接数", "个", "失败的连接数", MonitorItemType.NUMBER),
				new MonitorItem("RESIN-CONNPOOL","RESIN-CONNPOOL-7", "最后一次失败时间", "", "最后一次连接的失败时间", MonitorItemType.TEXT),
				new MonitorItem("RESIN-CONNPOOL","RESIN-CONNPOOL-8", "最大连接数", "个", "最大的连接数", MonitorItemType.NUMBER),
				new MonitorItem("RESIN-CONNPOOL","RESIN-CONNPOOL-9", "最长空闲时间", "秒", "最长空闲时间", MonitorItemType.NUMBER),
				new MonitorItem("RESIN-RATIO", "RESIN-RATIO-1", "命中率", "%", "Resin服务的命中率", MonitorItemType.NUMBER),
				new MonitorItem("RESIN-RATIO", "RESIN-RATIO-2", "命中次数", "个", "Resin服务的命中次数", MonitorItemType.NUMBER),
				new MonitorItem("RESIN-RATIO", "RESIN-RATIO-3", "总次数", "个", "Resin服务的总次数", MonitorItemType.NUMBER),
				new MonitorItem("RESIN-WEBAPP", "RESIN-WEBAPP-1", "状态", "", "应用的状态", MonitorItemType.TEXT),
				new MonitorItem("RESIN-WEBAPP", "RESIN-WEBAPP-2", "请求数", "个", "请求连接数", MonitorItemType.NUMBER),
				new MonitorItem("RESIN-WEBAPP", "RESIN-WEBAPP-3", "会话数", "个", "会话活跃数", MonitorItemType.NUMBER),
				new MonitorItem("RESIN-WEBAPP", "RESIN-WEBAPP-4", "开始时间", "", "应用启用时间", MonitorItemType.TEXT),
				new MonitorItem("RESIN-WEBAPP", "RESIN-WEBAPP-5", "状态500的总数", "个", "响应为状态500的总数", MonitorItemType.NUMBER),
				new MonitorItem("RESIN-WEBAPP", "RESIN-WEBAPP-6", "状态500的最后一次时间", "", "响应为状态500的最后一次时间", MonitorItemType.TEXT) };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(ResinJMXOption.TYPE_ID, "Resin Agent监测协议",
				ResinJMXPanel.class) };
	}
}
