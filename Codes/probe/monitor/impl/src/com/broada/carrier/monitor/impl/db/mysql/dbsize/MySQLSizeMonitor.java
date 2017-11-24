package com.broada.carrier.monitor.impl.db.mysql.dbsize;

import com.broada.carrier.monitor.impl.db.mysql.MySQLException;
import com.broada.carrier.monitor.impl.db.mysql.MySQLService;
import com.broada.carrier.monitor.impl.db.mysql.MySQLUtil;
import com.broada.carrier.monitor.impl.db.mysql.impl.DefaultMySQLService;
import com.broada.carrier.monitor.impl.db.mysql.util.MySQLErrorUtil;
import com.broada.carrier.monitor.method.mysql.MySQLMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

import java.util.List;
import java.util.Map;

public class MySQLSizeMonitor implements Monitor {
	private static final String ITEMIDX_MYSQL_SIZE = "MySQL-SIZE-1";

	@Override
	public MonitorResult collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		MySQLMonitorMethodOption method = new MySQLMonitorMethodOption(context.getMethod());

		MySQLService mySQLService = new DefaultMySQLService(context.getNode().getIp(), method);
		long time = System.currentTimeMillis();
		try {
			mySQLService.initConnection();
			List databases = mySQLService.showDatabases();
			for (int i = 0; i < databases.size(); i++) {
				MonitorResultRow row = new MonitorResultRow(databases.get(i).toString());
				result.addRow(row);
			}
		} catch (MySQLException e) {
			return MySQLErrorUtil.process(e, time, result);
		} finally {
			mySQLService.close();
		}

		return result;
	}

	@Override
	public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult();
		MySQLMonitorMethodOption method = new MySQLMonitorMethodOption(context.getMethod());
		MySQLService mySQLService = new DefaultMySQLService(context.getNode().getIp(), method);
		long time = System.currentTimeMillis();
		try {
			mySQLService.initConnection();
			Map<String, Long> sizeMap = mySQLService.getAllDatabaseSize();
			for (Map.Entry<String, Long> entry : sizeMap.entrySet()) {
				MonitorResultRow row = new MonitorResultRow(entry.getKey());

				row.setIndicator(ITEMIDX_MYSQL_SIZE, MySQLUtil.b2M(entry.getValue()));
				result.addRow(row);
			}
		} catch (MySQLException e) {
			return MySQLErrorUtil.process(e, time, result);
		} finally {
			mySQLService.close();
		}

		return result;
	}
}