package com.broada.carrier.monitor.impl.db.postgresql;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.db.postgresql.basic.PostgreSQLBaseMonitor;
import com.broada.carrier.monitor.impl.db.postgresql.session.PostgreSQLSessionMonitor;
import com.broada.carrier.monitor.impl.db.postgresql.tablespace.PostgreSQLTableSpaceMonitor;
import com.broada.carrier.monitor.method.postgresql.PostgreSQLConfPanel;
import com.broada.carrier.monitor.method.postgresql.PostgreSQLMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class PostgreSQLMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "PostgreSQL" };
		String[] methodTypeIds = new String[] { PostgreSQLMonitorMethodOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("POSTGRESQL", "POSTGRE-SESSION", "POSTGRESQL会话监测器", "监测POSTGRESQL会话信息",
						PostMonitorConfiger.class.getName(), PostgreSQLSessionMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("POSTGRESQL", "POSTGRE-TABLESPACE", "POSTGRESQL表空间监测器", "监测POSTGRESQL表空间的使用情况",
						MultiInstanceConfiger.class.getName(), PostgreSQLTableSpaceMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("POSTGRESQL", "POSTGRESQL-BASE", "PostgreSQL基础监测 [可用性]",
						"PostgreSQL基础监测器,可监测PostgreSQL的基本运行信息.", SingleInstanceConfiger.class.getName(),
						PostgreSQLBaseMonitor.class.getName(), index++, targetTypeIds, methodTypeIds), };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] { new MonitorItem("POSTGRE-SESSION", "POSTGRE-SESSION-1", "用户名", "", "当前登录的用户名", MonitorItemType.TEXT),
				new MonitorItem("POSTGRE-SESSION", "POSTGRE-SESSION-2", "ip地址", "", "用户的ip地址", MonitorItemType.TEXT),
				new MonitorItem("POSTGRE-SESSION", "POSTGRE-SESSION-3", "端口", "", "用户的端口", MonitorItemType.TEXT),
				new MonitorItem("POSTGRE-SESSION", "POSTGRE-SESSION-4", "连接时间", "", "用户登录的时间", MonitorItemType.TEXT),

				new MonitorItem("POSTGRE-TABLESPACE","POSTGRE-TABLESPACE-1", "大小", "MB", "表空间的大小", MonitorItemType.NUMBER),

				new MonitorItem("POSTGRESQL-BASE", "POSTGRESQL-BASE-1", "主机名", "", "主机名", MonitorItemType.TEXT),
				new MonitorItem("POSTGRESQL-BASE", "POSTGRESQL-BASE-2", "数据库名称", "", "数据库名称", MonitorItemType.TEXT),
				new MonitorItem("POSTGRESQL-BASE", "POSTGRESQL-BASE-3", "端口号", "", "端口号", MonitorItemType.TEXT),
				new MonitorItem("POSTGRESQL-BASE", "POSTGRESQL-BASE-4", "数据版本号", "", "数据版本号", MonitorItemType.TEXT),
				new MonitorItem("POSTGRESQL-BASE", "POSTGRESQL-BASE-5", "版本字符串", "", "版本字符串", MonitorItemType.TEXT),
				new MonitorItem("POSTGRESQL-BASE", "POSTGRESQL-BASE-6", "系统最后使用的oid", "", "系统最后使用的oid", MonitorItemType.TEXT),
				new MonitorItem("POSTGRESQL-BASE", "POSTGRESQL-BASE-7", "最近启动时间", "", "最近启动时间", MonitorItemType.TEXT),
				new MonitorItem("POSTGRESQL-BASE", "POSTGRESQL-BASE-8", "autovacuum是否启动", "", "autovacuum是否启动", MonitorItemType.TEXT),
				new MonitorItem("POSTGRESQL-BASE", "POSTGRESQL-BASE-9", "会话数", "", "会话数", MonitorItemType.NUMBER), };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(PostgreSQLMonitorMethodOption.TYPE_ID,
				"PostgreSQL JDBC 监测协议", PostgreSQLConfPanel.class), };
	}

}
