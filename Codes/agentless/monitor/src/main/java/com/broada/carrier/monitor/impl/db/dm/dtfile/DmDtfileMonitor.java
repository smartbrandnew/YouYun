package com.broada.carrier.monitor.impl.db.dm.dtfile;

import java.io.Serializable;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.dm.DmManager;
import com.broada.carrier.monitor.method.dm.DmMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * Dm数据文件监测
 * 文件名、列名作为key的可行性确认
 * 
 * @author Zhouqa
 * Create By 2016年4月7日 下午3:46:28
 */
public class DmDtfileMonitor implements Monitor {
	private static final Log logger = LogFactory.getLog(DmDtfileMonitor.class);
	private static final int ITEMIDX_LENGTH = 4;
	
	private Double getDtValue(DmDtfileInfo dtFile, int index) {
		switch (index) {
		case 0:
			return dtFile.getDfRTS();
		case 1:
			return dtFile.getDfWRTS();
		case 2:
			return dtFile.getDfTotalSize();
		case 3:
			return dtFile.getDfFreeSize();
		default:
			return -1.0;
		}
	}
	
	@Override
	public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);

		DmMonitorMethodOption option = new DmMonitorMethodOption(context.getMethod());
		DmManager dm = new DmManager(context.getNode().getIp(), option);
		long respTime = System.currentTimeMillis();
		try {
			dm.initConnection();
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
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(errMsg);
			dm.close();
			return result;
		}
		//数据文件列表获取
		List<DmDtfileInfo> dtFileList;
		try {
			dtFileList = dm.getAllDtfiles();
		} catch (Exception e) {
			String errMsg = "无法获取数据库数据文件.";
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(errMsg);
			return result;
		} finally {
			dm.close();
		}
		respTime = System.currentTimeMillis() - respTime;
		if (respTime <= 0) {
			respTime = 1;
		}
		result.setResponseTime(respTime);

		for (DmDtfileInfo file : dtFileList) {
			MonitorResultRow row = new MonitorResultRow();
			row.setInstCode(file.getDfName());
			row.setInstName(file.getDfName());

			for (int j = 0; j < ITEMIDX_LENGTH; j++) {
				row.setIndicator("DM-DATAFILE-" + (j + 1), getDtValue(file, j));
			}
			result.addRow(row);
		}
		return result;
	}

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setMessage("采集指标开始...");
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		DmMonitorMethodOption option = new DmMonitorMethodOption(context.getMethod());
		DmManager dm = new DmManager(context.getNode().getIp(), option);
		long respTime = System.currentTimeMillis();
		try {
			dm.initConnection();
			result.setMessage("采集指标进行中....");
		} catch (LoginException lde) {
			result.setState(MonitorState.FAILED);
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
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(MonitorState.FAILED);
			result.setResultDesc(errMsg);
			dm.close();
			return result;
		}
		//数据文件列表获取
		List<DmDtfileInfo> dtFileList;
		try {
			dtFileList = dm.getAllDtfiles();
		} catch (Exception e) {
			String errMsg = "无法获取数据库数据文件.";
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(MonitorState.FAILED);
			result.setResultDesc(errMsg);
			return result;
		} finally {
			dm.close();
		}
		respTime = System.currentTimeMillis() - respTime;
		if (respTime <= 0) {
			respTime = 1;
		}
		result.setResponseTime(respTime);

		for (DmDtfileInfo file : dtFileList) {
			MonitorResultRow row = new MonitorResultRow();
			row.setInstCode(file.getDfName());
			row.setInstName(file.getDfName());

			for (int j = 0; j < ITEMIDX_LENGTH; j++) {
				row.setIndicator("DM-DATAFILE-" + (j + 1), getDtValue(file, j));
				result.setMessage("采集指标进行中....");
			}
			result.addRow(row);
		}
		result.setMessage("采集工作完成。");
		result.setState(MonitorState.SUCCESSED);
		return result;
	}

}
