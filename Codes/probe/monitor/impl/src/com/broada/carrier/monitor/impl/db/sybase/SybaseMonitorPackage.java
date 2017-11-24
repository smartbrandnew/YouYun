package com.broada.carrier.monitor.impl.db.sybase;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.db.sybase.basic.SybaseBasicParamMonitor;
import com.broada.carrier.monitor.impl.db.sybase.database.SybaseDatabaseMonitor;
import com.broada.carrier.monitor.impl.db.sybase.segment.SybaseSegmentMonitor;
import com.broada.carrier.monitor.impl.db.sybase.session.SybaseSessionMonitor;
import com.broada.carrier.monitor.impl.db.sybase.transaction.SybaseTransactionMonitor;
import com.broada.carrier.monitor.method.sybase.SybaseConfPanel;
import com.broada.carrier.monitor.method.sybase.SybaseMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class SybaseMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "Sybase" };
		String[] methodTypeIds = new String[] { SybaseMonitorMethodOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("SYBASE", "SYBASE-BASIC", "Sybase基本信息采集 [可用性]", "采集Sybase数据库的基本信息。",
						SingleInstanceConfiger.class.getName(), SybaseBasicParamMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				new MonitorType("SYBASE", "SYBASE-DATABASE", "Sybase数据库监测", "监测Sybase数据库实例的使用情况。",
						MultiInstanceConfiger.class.getName(), SybaseDatabaseMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				new MonitorType("SYBASE", "SYBASE-SEGMENT", "Sybase数据段监测", "监测Sybase数据段",
						MultiInstanceConfiger.class.getName(), SybaseSegmentMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				new MonitorType("SYBASE", "SYBASE-SESSION", "Sybase会话监测", "监测Sybase会话",
						MultiInstanceConfiger.class.getName(), SybaseSessionMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				new MonitorType("SYBASE", "SYBASE-TRANSACTION", "Sybase事务监测", "监测Sybase数据库事务情况。",
						SingleInstanceConfiger.class.getName(), SybaseTransactionMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds) };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("SYBASE-BASIC","SYBASE-BASIC-1", "DBMS名称", "", "数据库管理系统名称", MonitorItemType.TEXT),
				new MonitorItem("SYBASE-BASIC","SYBASE-BASIC-2", "DBMS版本", "", "数据库管理系统版本", MonitorItemType.TEXT),
				new MonitorItem("SYBASE-BASIC","SYBASE-BASIC-3", "产品名称", "", "数据库产品名称", MonitorItemType.TEXT),
				new MonitorItem("SYBASE-BASIC","SYBASE-BASIC-4", "产品版本", "", "数据库产品版本", MonitorItemType.TEXT),
				new MonitorItem("SYBASE-BASIC","SYBASE-BASIC-5", "主版本号", "", "主版本号", MonitorItemType.TEXT),
				new MonitorItem("SYBASE-BASIC","SYBASE-BASIC-6", "次版本号", "", "次版本号", MonitorItemType.TEXT),
				new MonitorItem("SYBASE-BASIC","SYBASE-BASIC-7", "系统信息", "", "系统信息", MonitorItemType.TEXT),

				new MonitorItem("SYBASE-DATABASE","SYBASE-DATABASE-1", "空间大小", "MB", "指定Sybase数据库实例的总空间大小", MonitorItemType.NUMBER),
				new MonitorItem("SYBASE-DATABASE","SYBASE-DATABASE-2", "数据大小", "MB", "指定Sybase数据库实例的数据大小", MonitorItemType.NUMBER),
				new MonitorItem("SYBASE-DATABASE","SYBASE-DATABASE-3", "索引大小", "MB", "指定Sybase数据库实例的索引大小", MonitorItemType.NUMBER),
				new MonitorItem("SYBASE-DATABASE","SYBASE-DATABASE-4", "已使用大小", "MB", "指定Sybase数据库实例的已使用空间大小", MonitorItemType.NUMBER),
				new MonitorItem("SYBASE-DATABASE","SYBASE-DATABASE-5", "使用百分比", "%", "指定Sybase数据库实例的已使用空间百分比", MonitorItemType.NUMBER),

				new MonitorItem("SYBASE-SEGMENT", "SYBASE-SEGMENT-1", "段总大小", "MB", "段总大小", MonitorItemType.NUMBER),
				new MonitorItem("SYBASE-SEGMENT", "SYBASE-SEGMENT-2", "数据段大小", "MB", "数据段大小", MonitorItemType.NUMBER),
				new MonitorItem("SYBASE-SEGMENT", "SYBASE-SEGMENT-3", "索引段大小", "MB", "索引段大小", MonitorItemType.NUMBER),
				new MonitorItem("SYBASE-SEGMENT", "SYBASE-SEGMENT-4", "未使用段大小", "MB", "未使用段大小", MonitorItemType.NUMBER),

				new MonitorItem("SYBASE-SESSION","SYBASE-SESSION-1", "状态", "", "状态", MonitorItemType.TEXT),
				new MonitorItem("SYBASE-SESSION","SYBASE-SESSION-2", "用户", "", "用户", MonitorItemType.TEXT),
				new MonitorItem("SYBASE-SESSION","SYBASE-SESSION-3", "主机", "", "主机", MonitorItemType.TEXT),
				new MonitorItem("SYBASE-SESSION","SYBASE-SESSION-4", "程序", "", "程序", MonitorItemType.TEXT),
				new MonitorItem("SYBASE-SESSION","SYBASE-SESSION-5", "数据库", "", "数据库", MonitorItemType.TEXT),
				new MonitorItem("SYBASE-SESSION","SYBASE-SESSION-6", "命令", "", "命令", MonitorItemType.TEXT),
				new MonitorItem("SYBASE-SESSION","SYBASE-SESSION-7", "已用内存", "KB", "已用内存", MonitorItemType.NUMBER),
				new MonitorItem("SYBASE-SESSION","SYBASE-SESSION-8", "CPU时间", "ms", "CPU时间", MonitorItemType.NUMBER),
				new MonitorItem("SYBASE-SESSION","SYBASE-SESSION-9", "IO读写次数", "次", "IO读写次数", MonitorItemType.NUMBER),

				new MonitorItem("SYBASE-TRANSACTION", "SYBASE-TRANSACTION-1", "系统事务交易总数", "个", "提供Sybase数据库启动以来事务数", MonitorItemType.NUMBER),
				new MonitorItem("SYBASE-TRANSACTION", "SYBASE-TRANSACTION-2", "每秒事务数", "个/秒", "提供Sybase数据库实例的每秒提交事务数", MonitorItemType.NUMBER),
				new MonitorItem("SYBASE-TRANSACTION", "SYBASE-TRANSACTION-3", "每秒回滚事务数", "个/秒", "提供Sybase数据库实例的每秒回滚事务数",
						MonitorItemType.NUMBER), };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(SybaseMonitorMethodOption.TYPE_ID, "Sybase JDBC 监测协议",
				SybaseConfPanel.class), };
	}

}
