package com.broada.carrier.monitor.impl.db.postgresql.basic;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.postgresql.PostgreSQLException;
import com.broada.carrier.monitor.impl.db.postgresql.impl.DefaultPostgreSQLService;
import com.broada.carrier.monitor.method.postgresql.PostgreSQLMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Map;

public class PostgreSQLBaseMonitor extends BaseMonitor {
	private static final Log logger = LogFactory.getLog(PostgreSQLBaseMonitor.class);

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		PostgreSQLMonitorMethodOption option = new PostgreSQLMonitorMethodOption(context.getMethod());

		// 连接参数的获取
		String ip = context.getNode().getIp();
		int port = option.getPort();
		String sid = option.getDb();
		String user = option.getUsername();
		String pass = option.getPassword();

		DefaultPostgreSQLService om = new DefaultPostgreSQLService(ip, sid, port, user, pass);
		long replyTime = 0;
		long time = System.currentTimeMillis();
		try {
			om.initConnection();
		} catch (PostgreSQLException e) {
			if (e.getSQLState().equals(PostgreSQLException.ERRCODE_DWPRESU)) {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				result.setResultDesc(e.getMessage());
				// 认为可以连接,所以有连接时间
				replyTime = System.currentTimeMillis() - time;
				if (replyTime <= 0) {
					replyTime = 1;
				}
				result.setResponseTime((int) replyTime);
			} else {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				result.setResultDesc(e.getMessage());
			}
			om.close();
			return result;
		}

		Map<String, String> postgreBase = null;
		try {
			postgreBase = om.getBasicInfo();
		} catch (PostgreSQLException e) {
			String errMsg = "无法获取数据库基本配置信息.\n postgresql错误号:" + e.getSQLState();
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(errMsg);
			return result;
		} finally {
			om.close();
		}
		replyTime = System.currentTimeMillis() - time;
		if (replyTime <= 0) {
			replyTime = 1;
		}
		result.setResponseTime(replyTime);

		MonitorResultRow row = new MonitorResultRow();

		for (int i = 0; i < PostgreSQLBaseParamConfiger.keys.length; i++) {
			String value = String.valueOf(postgreBase.get(PostgreSQLBaseParamConfiger.keys[i]));
			if (i == 8)
				row.setIndicator("POSTGRESQL-BASE-" + (i + 1), Integer.parseInt(value));
			else
				row.setIndicator("POSTGRESQL-BASE-" + (i + 1), value);
		}
		result.addRow(row);
		result.setState(MonitorConstant.MONITORSTATE_NICER);
		return result;
	}
}
