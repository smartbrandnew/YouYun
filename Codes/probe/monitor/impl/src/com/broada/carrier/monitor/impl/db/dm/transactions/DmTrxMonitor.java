package com.broada.carrier.monitor.impl.db.dm.transactions;

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

public class DmTrxMonitor extends BaseMonitor {
	private static Log log = LogFactory.getLog(DmTrxMonitor.class);

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

		List<DmTrx> trxList = null;
		try {
			long replyTime = System.currentTimeMillis();
			trxList = dm.getAllTrx();
			replyTime = System.currentTimeMillis() - replyTime;
			if (replyTime <= 0)
				replyTime = 1L;
			result.setResponseTime(replyTime);
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("无法获取数据库事务信息.", e);
			}
			result.setResultDesc("无法获取数据库事务信息.");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		} finally {
			dm.close();
		}
		int index = 1;
		for (DmTrx trx : trxList) {
			MonitorResultRow row = new MonitorResultRow("TRX" + index++);
			row.setIndicator("DM-TRX-1", trx.getTrID());
			row.setIndicator("DM-TRX-2", trx.getState());
			row.setIndicator("DM-TRX-3", trx.getIsolation());
			row.setIndicator("DM-TRX-4", trx.getReadOnly());
			row.setIndicator("DM-TRX-5", trx.getSessID());
			row.setIndicator("DM-TRX-6", trx.getInsCnt());
			row.setIndicator("DM-TRX-7", trx.getDelCnt());
			row.setIndicator("DM-TRX-8", trx.getUptCnt());
			row.setIndicator("DM-TRX-9", trx.getUptInsCnt());
			row.setIndicator("DM-TRX-10", trx.getUrecSeq());
			row.setIndicator("DM-TRX-11", trx.getWait());
			result.addRow(row);
		}
		return result;
	}

}
