package com.broada.carrier.monitor.impl.db.dm.sql;

import java.io.Serializable;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.dm.DmManager;
import com.broada.carrier.monitor.method.dm.DmMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class DmSQLMonitor extends BaseMonitor {
	private static Log log = LogFactory.getLog(DmSQLMonitor.class);

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		DmMonitorMethodOption option = new DmMonitorMethodOption(context.getMethod());
		DmManager dm = new DmManager(context.getNode().getIp(), option);
		long respTime = System.currentTimeMillis();
		try {
			dm.initConnection();
			respTime = System.currentTimeMillis() - respTime;
			if (respTime <= 0) {
				respTime = 1;
			}
			result.setResponseTime(respTime);
		} catch (LoginException lde) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(lde.getMessage());
			respTime = System.currentTimeMillis() - respTime;
			if (respTime <= 0) {
				respTime = 1;
			}
			result.setResponseTime(respTime);
			dm.close();
			return result;
		} catch (Exception e) {
			String errMsg = e.getMessage();
			if (log.isDebugEnabled()) {
				log.debug(errMsg, e);
			}
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(errMsg);
			dm.close();
			return result;
		}

		List<DmSQLText> sqlList = null;
		try {
			long replyTime = System.currentTimeMillis();
			sqlList = dm.getAllSQLS();
			replyTime = System.currentTimeMillis() - replyTime;
			if (replyTime <= 0)
				replyTime = 1L;
			result.setResponseTime(replyTime);
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("无法获取数据库SQL信息.", e);
			}
			result.setResultDesc("无法获取数据库SQL信息.");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		} finally {
			dm.close();
		}
		int index = 1;
		for (DmSQLText sql : sqlList) {
			MonitorResultRow row = new MonitorResultRow("SQL" + index++);
			row.setIndicator("DM-SQL-1", sql.getSeqNo());
			row.setIndicator("DM-SQL-2", sql.getSessID());
			row.setIndicator("DM-SQL-3", sql.getTrxID());
			row.setIndicator("DM-SQL-4", sql.getSqlText());
			row.setIndicator("DM-SQL-5", sql.getStartTime());
			row.setIndicator("DM-SQL-6", sql.getTimeUsed());
			row.setIndicator("DM-SQL-7", sql.getIsOver());
			row.setIndicator("DM-SQL-8", sql.getUserName());
			row.setIndicator("DM-SQL-9", sql.getClntIp());
			row.setIndicator("DM-SQL-10", sql.getAppName());

			result.addRow(row);
		}
		return result;
	}

}
