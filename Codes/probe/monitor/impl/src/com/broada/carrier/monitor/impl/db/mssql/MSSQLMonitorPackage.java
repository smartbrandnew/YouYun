package com.broada.carrier.monitor.impl.db.mssql;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.db.mssql.basic.BasicInfoMonitor;
import com.broada.carrier.monitor.impl.db.mssql.database.DatabaseMonitor;
import com.broada.carrier.monitor.impl.db.mssql.file.FileMonitor;
import com.broada.carrier.monitor.impl.db.mssql.session.SessionMonitor;
import com.broada.carrier.monitor.method.mssql.MSSQLConfPanel;
import com.broada.carrier.monitor.method.mssql.MSSQLMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class MSSQLMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "SQLServer" };
		String[] methodTypeIds = new String[] { MSSQLMonitorMethodOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("MSSQL", "MSSQL-BASIC", "SQLServer基本信息 [可用性]", "监测Microsoft SQL Server数据库数据的基本信息。",
						SingleInstanceConfiger.class.getName(), BasicInfoMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("MSSQL", "MSSQL-DBSIZE", "SQLServer数据库大小", "监测Microsoft SQL Server数据库的大小。",
						MultiInstanceConfiger.class.getName(), DatabaseMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),

				new MonitorType("MSSQL", "MSSQL-FILE", "SQLServer文件监测", "监测Microsoft SQL Server数据库数据文件的大小。",
						MultiInstanceConfiger.class.getName(), FileMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),

				new MonitorType("MSSQL", "MSSQL-SESSION", "SQLServer会话监测",
						"监测Microsoft SQL Server数据库会话占用的CPU时间，内存使用量，只对running状态的会话做监测。",
						MultiInstanceConfiger.class.getName(), SessionMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds), };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("MSSQL-BASIC","MSSQL-BASIC-1", "Windows版本", "", "Windows版本", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-BASIC","MSSQL-BASIC-2", "处理器名称", "", "处理器名称", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-BASIC","MSSQL-BASIC-3", "处理器数目", "", "处理器数目", MonitorItemType.NUMBER),
				new MonitorItem("MSSQL-BASIC","MSSQL-BASIC-4", "主机名", "", "主机名", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-BASIC","MSSQL-BASIC-5", "内存(MB)", "", "内存大小", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-BASIC","MSSQL-BASIC-6", "数据库数目", "个", "数据库个数", MonitorItemType.NUMBER),
				new MonitorItem("MSSQL-BASIC","MSSQL-BASIC-7", "会话数目", "个", "会话个数", MonitorItemType.NUMBER),
				new MonitorItem("MSSQL-BASIC","MSSQL-BASIC-8", "启动时间", "", "启动时间", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-BASIC","MSSQL-BASIC-9", "阻塞进程数", "个", "阻塞进程数", MonitorItemType.NUMBER),
				new MonitorItem("MSSQL-BASIC","MSSQL-BASIC-10", "数据库版本", "", "数据库版本", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-BASIC","MSSQL-BASIC-11", "数据文件大小", "MB", "指定数据库的数据文件的大小", MonitorItemType.NUMBER),
				new MonitorItem("MSSQL-BASIC","MSSQL-BASIC-12", "日志文件大小", "MB", "指定数据库的日志文件的大小", MonitorItemType.NUMBER),
				new MonitorItem("MSSQL-BASIC","MSSQL-BASIC-13", "CPU使用率", "%", "SQLServer数据库处理时占用服务器CPU总时间的百分比",
						MonitorItemType.NUMBER),
				new MonitorItem("MSSQL-BASIC","MSSQL-BASIC-14", "连接会话数", "个", "连接到数据库的会话数", MonitorItemType.NUMBER),

				new MonitorItem("MSSQL-DBSIZE","MSSQL-DBSIZE-1", "数据库大小", "MB", "数据库大小", MonitorItemType.NUMBER),
				new MonitorItem("MSSQL-DBSIZE","MSSQL-DBSIZE-2", "数据大小", "MB", "数据大小", MonitorItemType.NUMBER),
				new MonitorItem("MSSQL-DBSIZE","MSSQL-DBSIZE-3", "索引大小", "MB", "索引大小", MonitorItemType.NUMBER),
				new MonitorItem("MSSQL-DBSIZE","MSSQL-DBSIZE-4", "未使用大小", "MB", "未使用大小", MonitorItemType.NUMBER),
				new MonitorItem("MSSQL-DBSIZE","MSSQL-DBSIZE-5", "未分配大小", "MB", "未分配大小", MonitorItemType.NUMBER),
				new MonitorItem("MSSQL-DBSIZE","MSSQL-DBSIZE-6", "保留大小", "MB", "保留大小", MonitorItemType.NUMBER),

				new MonitorItem("MSSQL-FILE","MSSQL-FILE-1", "数据库", "", "数据库名称", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-FILE","MSSQL-FILE-2", "文件组", "", "数据库文件所属组", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-FILE","MSSQL-FILE-3", "大小", "MB", "文件当前大小", MonitorItemType.NUMBER),
				new MonitorItem("MSSQL-FILE","MSSQL-FILE-4", "容量", "", "文件能达到的最大大小", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-FILE","MSSQL-FILE-5", "增长方式", "", "按比例还是按大小增长以及增长量", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-FILE","MSSQL-FILE-6", "路径", "", "文件存放的位置", MonitorItemType.TEXT),

				new MonitorItem("MSSQL-SESSION","MSSQL-SESSION-1", "状态", "", "会话状态", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-SESSION","MSSQL-SESSION-2", "用户", "", "占用该会话的用户", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-SESSION","MSSQL-SESSION-3", "主机", "", "建立会话的主机", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-SESSION","MSSQL-SESSION-4", "程序", "", "客户端程序", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-SESSION","MSSQL-SESSION-5", "内存(KB)", "KB", "会话占用的内存", MonitorItemType.NUMBER),
				new MonitorItem("MSSQL-SESSION","MSSQL-SESSION-6", "CPU时间(ms)", "ms", "会话占用的CPU时间", MonitorItemType.NUMBER),
				new MonitorItem("MSSQL-SESSION","MSSQL-SESSION-7", "数据库", "", "数据库名称", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-SESSION","MSSQL-SESSION-8", "命令", "", "会话执行的命令", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-SESSION","MSSQL-SESSION-9", "最后处理时间", "", "会话最后活动时间", MonitorItemType.TEXT),
				new MonitorItem("MSSQL-SESSION","MSSQL-SESSION-10", "建立时间", "", "建立会话的时间", MonitorItemType.TEXT), };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(MSSQLMonitorMethodOption.TYPE_ID, "SQLServer JDBC 监测协议",
				MSSQLConfPanel.class), };
	}

}
