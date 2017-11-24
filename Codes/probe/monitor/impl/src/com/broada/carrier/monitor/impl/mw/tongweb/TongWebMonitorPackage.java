package com.broada.carrier.monitor.impl.mw.tongweb;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.mw.tongweb.conn.TongWebConnMonitor;
import com.broada.carrier.monitor.impl.mw.tongweb.dbpool.TongWebDBPoolMonitor;
import com.broada.carrier.monitor.impl.mw.tongweb.dbpool.TongWebDBPoolMonitorVer4;
import com.broada.carrier.monitor.method.tongweb.TongWebMonitorMethodOption;
import com.broada.carrier.monitor.method.tongweb.TongWebParamPanel;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

/**
 * Created by hu on 2015/5/18.
 */
public class TongWebMonitorPackage implements MonitorPackage {
	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "TongWeb" };
		String[] methodTypeIds = new String[] { TongWebMonitorMethodOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("TONGWEB", "TONGWEB-CONN", "连接信息采集 [可用性]",
						"监测TongWeb的连接信息,每个端口作为一个实例来展示,另外监测项显示的默认值是当前监测器监测出来的值,如果用户不做改,则以默认值做为监测项的阈值(每次监测出来的值小于阈值则告警).",
						MultiInstanceConfiger.class.getName(), TongWebConnMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				new MonitorType(
						"TONGWEB",
						"TONGWEB-DBPOOL",
						"数据库连接池信息采集(JCA)",
						"监测基于JCA数据源的数据库连接池信息,每个连接池作为一个实例,监测项显示的默认值是当前监测器监测出来的值,如果用户不做改,则以默认值做为监测项的阈值(每次监测出来的值大于阈值则告警).",
						MultiInstanceConfiger.class.getName(), TongWebDBPoolMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				new MonitorType("TONGWEB", "TONGWEB4-DBPOOL", "数据库连接池信息采集(版本4.X)",
						"监测数据库连接池信息,每个连接池作为一个实例,监测项显示的默认值是当前监测器监测出来的值,如果用户不做改,则以默认值做为监测项的阈值(每次监测出来的值大于阈值则告警).",
						MultiInstanceConfiger.class.getName(), TongWebDBPoolMonitorVer4.class.getName(), index++,
						targetTypeIds, methodTypeIds), };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] { new MonitorItem("TONGWEB-CONN","TONGWEB-CONN-1", "等待线程数", "个", "等待线程数", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-CONN","TONGWEB-CONN-2", "活动线程数", "个", "活动线程数", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-CONN","TONGWEB-CONN-3", "最大处理线程数", "个", "最大处理线程数", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-CONN","TONGWEB-CONN-4", "请求等待队列大小", "个", "请求等待队列大小", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-CONN","TONGWEB-CONN-5", "客户端超时", "秒", "客户端超时", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-CONN","TONGWEB-CONN-6", "处理线程等待请求超时", "秒", "处理线程在等待一个请求过程中的超时时间", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-CONN","TONGWEB-CONN-7", "等待处理请求数", "个", "当前等待处理的请求数", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-CONN","TONGWEB-CONN-8", "吞吐率", "次/分钟", "单位时间内接收和响应请求的次数", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-CONN","TONGWEB-CONN-9", "每秒发送字节数", "KB/秒", "单位时间内的发送字节数", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-CONN","TONGWEB-CONN-10", "每秒接收字节数", "KB/秒", "单位时间内的接收字节数", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-DBPOOL","TONGWEB-DBPOOL-1", "最大连接数", "个", "最大连接数", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-DBPOOL","TONGWEB-DBPOOL-2", "最小连接数", "个", "最小连接数", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-DBPOOL","TONGWEB-DBPOOL-3", "当前连接数", "个", "当前连接数", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-DBPOOL","TONGWEB-DBPOOL-4", "当前活动连接数", "个", "当前活动连接数", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-DBPOOL","TONGWEB-DBPOOL-5", "创建的连接数", "个", "创建的连接数", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-DBPOOL","TONGWEB-DBPOOL-6", "关闭的连接数", "个", "关闭的连接数", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-DBPOOL","TONGWEB-DBPOOL-7", "可用连接数", "个", "可用连接数", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-DBPOOL","TONGWEB-DBPOOL-8", "最大使用连接数", "个", "最大使用连接数", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-DBPOOL","TONGWEB-DBPOOL-9", "连接阻塞超时时间", "毫秒", "连接阻塞超时时间", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-DBPOOL","TONGWEB-DBPOOL-10", "连接闲置时间", "分钟", "连接闲置时间", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-DBPOOL","TONGWEB-DBPOOL-11", "连接超时时间", "分钟", "连接超时时间", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-DBPOOL","TONGWEB-DBPOOL-12", "连接状态代码", "", "连接状态代码", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB-DBPOOL","TONGWEB-DBPOOL-13", "连接状态描述", "", "连接状态描述", MonitorItemType.TEXT),

				new MonitorItem("TONGWEB4-DBPOOL","TONGWEB4-DBPOOL-1", "连接池容量", "个", "连接池容量", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB4-DBPOOL","TONGWEB4-DBPOOL-2", "当前活动连接数", "个", "当前活动连接数", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB4-DBPOOL","TONGWEB4-DBPOOL-3", "最小连接池容量", "个", "最小连接池容量", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB4-DBPOOL","TONGWEB4-DBPOOL-4", "最大连接池容量", "个", "最大连接池容量", MonitorItemType.NUMBER),
				new MonitorItem("TONGWEB4-DBPOOL","TONGWEB4-DBPOOL-5", "连接超时时间", "分钟", "连接超时时间", MonitorItemType.NUMBER), };

	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(TongWebMonitorMethodOption.TYPE_ID, "TongWeb Agent监测协议",
				TongWebParamPanel.class) };
	}
}
