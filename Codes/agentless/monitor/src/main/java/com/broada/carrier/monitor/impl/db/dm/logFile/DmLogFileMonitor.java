package com.broada.carrier.monitor.impl.db.dm.logFile;

import java.io.Serializable;
import java.math.BigDecimal;
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

public class DmLogFileMonitor extends BaseMonitor {
	private static Log log = LogFactory.getLog(DmLogFileMonitor.class);

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

		List<DmLogFile> logList = null;
		try {
			long replyTime = System.currentTimeMillis();
			logList = dm.getAllLogs();
			replyTime = System.currentTimeMillis() - replyTime;
			if (replyTime <= 0)
				replyTime = 1L;
			result.setResponseTime(replyTime);
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("无法获取数据库日志文件信息.", e);
			}
			result.setResultDesc("无法获取数据库日志文件信息.");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		} finally {
			dm.close();
		}

		for (DmLogFile log : logList) {
			MonitorResultRow row = new MonitorResultRow();
			row.setIndicator("DM-LOGFILES-1", log.getCkptLsn());
			row.setIndicator("DM-LOGFILES-2", log.getFileLsn());
			row.setIndicator("DM-LOGFILES-3", log.getFileLsn());
			row.setIndicator("DM-LOGFILES-4", log.getCurrLsn());
			row.setIndicator("DM-LOGFILES-5", log.getNextSeq());
			row.setIndicator("DM-LOGFILES-6", log.getMagic());
			row.setIndicator("DM-LOGFILES-7", log.getFlushPages());
			row.setIndicator("DM-LOGFILES-8", log.getFlushingPages());
			row.setIndicator("DM-LOGFILES-9", log.getCurrFile());
			row.setIndicator("DM-LOGFILES-10", log.getCurrOffset());
			row.setIndicator("DM-LOGFILES-11", log.getCkptFile());
			row.setIndicator("DM-LOGFILES-12", log.getCkptOffset());
			row.setIndicator("DM-LOGFILES-13", transToMb(log.getFreeSpace()));
			row.setIndicator("DM-LOGFILES-14", transToMb(log.getTotalSpace()));
			row.setIndicator("DM-LOGFILES-15", getRate(log.getFreeSpace(), log.getTotalSpace()));
			result.addRow(row);
		}
		return result;
	}

	private Double getRate(Double freeSpace, Double totalSpace) {
		BigDecimal bd = new BigDecimal(transToMb(freeSpace) / transToMb(totalSpace));
		bd = bd.setScale(5, BigDecimal.ROUND_HALF_UP);
		BigDecimal bd2 = new BigDecimal(1);
		return bd2.subtract(bd).multiply(new BigDecimal(100)).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	private Double transToMb(Double num) {
		return new BigDecimal(num).divide(new BigDecimal(1024 * 1024)).setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

}
