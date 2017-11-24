package com.broada.carrier.monitor.impl.db.mysql;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.db.mysql.basic.MySQLBaseMonitor;
import com.broada.carrier.monitor.impl.db.mysql.dbsize.MySQLSizeMonitor;
import com.broada.carrier.monitor.method.mysql.MySQLConfPanel;
import com.broada.carrier.monitor.method.mysql.MySQLMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class MySQLMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "MySQL" };
		String[] methodTypeIds = new String[] { MySQLMonitorMethodOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("MYSQL","MySQL-BASE", "MySQL基础监测 [可用性]", "MySQL基础监测器,可监测MySQL的运行状态,并获取各种运行参数和设定的条件进行比较,判断MySQL运行是否正常.",
						SingleInstanceConfiger.class.getName(), MySQLBaseMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),

				new MonitorType("MYSQL","MySQL-SIZE", "MySQL数据库大小监测", "监测MySQL数据库的大小,并根据设定的条件进行比较,不符合条件则告警,并可以保存每次监测结果值.",
						MultiInstanceConfiger.class.getName(), MySQLSizeMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds), };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("MySQL-BASE", "MySQL-BASE-1", "连接数", "个", "连接到MySQL数据库连接数", MonitorItemType.NUMBER),
				new MonitorItem("MySQL-BASE", "MySQL-BASE-2", "缓存查询数", "个", "缓存中保存的查询语句数目数", MonitorItemType.NUMBER),

				new MonitorItem("MySQL-SIZE", "MySQL-SIZE-1", "数据库大小", "MB", "数据库当前大小", MonitorItemType.NUMBER), };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] {
				new MonitorMethodType(MySQLMonitorMethodOption.TYPE_ID, "MySQL JDBC 监测协议", MySQLConfPanel.class), };
	}

}
