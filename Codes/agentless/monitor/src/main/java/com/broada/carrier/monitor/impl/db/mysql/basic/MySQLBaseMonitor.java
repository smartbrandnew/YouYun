package com.broada.carrier.monitor.impl.db.mysql.basic;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.mysql.MySQLException;
import com.broada.carrier.monitor.impl.db.mysql.MySQLService;
import com.broada.carrier.monitor.impl.db.mysql.impl.DefaultMySQLService;
import com.broada.carrier.monitor.impl.db.mysql.util.MySQLErrorUtil;
import com.broada.carrier.monitor.method.mysql.MySQLMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MySQLBaseMonitor extends BaseMonitor {

	private static Log log = LogFactory.getLog(MySQLBaseMonitor.class);
	private static Map<String, String> directory = new HashMap<String, String>();
	static {
		directory.put("THREADS_CONNECTED", "MySQL-BASE-1");
		directory.put("QCACHE_QUERIES_IN_CACHE", "MySQL-BASE-2");
	}

	@Override public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		MySQLMonitorMethodOption method = new MySQLMonitorMethodOption(context.getMethod());
		MySQLService mySQLService = new DefaultMySQLService(context.getNode().getIp(), method);
		long time = System.currentTimeMillis();
		try {
			mySQLService.initConnection();
			//获取数据库状态信息
			Map<String, String> statusMap;
			statusMap = mySQLService.getAllStatus();

			if (statusMap == null || statusMap.size() == 0) {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				result.setResultDesc("获取不到任何数据库状态信息.");
				return result;
			}

			MonitorResultRow row = new MonitorResultRow();
			for (Map.Entry<String, String> entry : directory.entrySet()) {
				String value = statusMap.get(entry.getKey());
				if (value == null) {
					continue;
				}
				row.setIndicator(entry.getValue(), value);
			}
			result.addRow(row);
			return result;
		} catch (MySQLException e) {
			log.error(e);
			return MySQLErrorUtil.process(e, time, result);
		} finally {
			mySQLService.close();
		}
	}
}
