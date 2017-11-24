package com.broada.carrier.monitor.impl.db.oracle.dtfile;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.oracle.exception.LogonDeniedException;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleManager;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.CollectMonitorState;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * Oracle 数据文件监测实现
 *  文件名.列名作为key的方式可行性确认
 * @author Lixy (lixy@broada.com.cn)
 * Create By 2006-10-20 10:16:57  
 */
public class OracleDtfileMonitor implements Monitor {

	private static final Log logger = LogFactory.getLog(OracleDtfileMonitor.class);
	public static final String SEPARATOR = "\u007F";

	private static final int ITEMIDX_LENGTH = 8;

	/**
	 * 取得index对应CLOUMNS引索列对应的监测结果值
	 * @param dtFileList
	 * @param dtFileName
	 * @param index
	 * @return
	 */
	private Double getDtValue(OracleDtfile dtFile, int index) {
		switch (index) {
		case 0:
			return dtFile.getDfRTS();
		case 1:
			return dtFile.getDfWRTS();
		case 2:
			return dtFile.getDfRTim();
		case 3:
			return dtFile.getDfWRTim();
		case 4:
			return dtFile.getDfSize();
		case 5:
			return dtFile.getPhyblkwrt();
		case 6:
			return dtFile.getPhyblkrd();
		case 7:
			return dtFile.getTotalBlock();
		default:
			return -1.0;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Serializable collect(CollectContext context) {
		CollectResult result = context.getResult();
		result.setProgress(1);
		result.setMessage("采集指标开始...");
		result.setState(CollectMonitorState.START);
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		OracleMethod option = new OracleMethod(context.getMethod());
		OracleManager om = new OracleManager(context.getNode().getIp(), option);
		long respTime = System.currentTimeMillis();
		try {
			om.initConnection();
			result.setMessage("采集指标进行中....");
			result.setState(CollectMonitorState.PROCESSING);
		} catch (LogonDeniedException lde) {
			result.setState(CollectMonitorState.FAILED);
			result.setResultDesc(lde.getMessage());
			respTime = System.currentTimeMillis() - respTime;
			if (respTime <= 0) {
				respTime = 1;
			}
			result.setResponseTime(respTime);
			om.close();
			return result;
		} catch (SQLException e) {
			String errMsg = e.getMessage();
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(CollectMonitorState.FAILED);
			result.setResultDesc(errMsg);
			om.close();
			return result;
		}
		//数据文件列表获取
		List<OracleDtfile> dtFileList;
		try {
			dtFileList = om.getAllDtfiles();
		} catch (SQLException e) {
			String errMsg = "无法获取数据库数据文件.";
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(CollectMonitorState.FAILED);
			result.setResultDesc(errMsg);
			return result;
		} finally {
			om.close();
		}
		respTime = System.currentTimeMillis() - respTime;
		if (respTime <= 0) {
			respTime = 1;
		}
		result.setResponseTime(respTime);

		for (OracleDtfile file : dtFileList) {
			MonitorResultRow row = new MonitorResultRow();
			row.setInstCode(file.getDfName());
			row.setInstName(file.getDfName());

			for (int j = 0; j < ITEMIDX_LENGTH; j++) {
				row.setIndicator("ORACLE-DATAFILE-" + (j + 1), getDtValue(file, j));
				result.setMessage("采集指标进行中....");
				result.setProgress(100 * (j + 1) / ITEMIDX_LENGTH);
				result.setState(CollectMonitorState.PROCESSING);
			}
			result.addRow(row);
		}
		result.setMessage("采集工作完成。");
		result.setProgress(100);
		result.setState(CollectMonitorState.SUCCESSED);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);

		OracleMethod option = new OracleMethod(context.getMethod());
		OracleManager om = new OracleManager(context.getNode().getIp(), option);
		long respTime = System.currentTimeMillis();
		try {
			om.initConnection();
		} catch (LogonDeniedException lde) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(lde.getMessage());
			respTime = System.currentTimeMillis() - respTime;
			if (respTime <= 0) {
				respTime = 1;
			}
			result.setResponseTime(respTime);
			om.close();
			return result;
		} catch (SQLException e) {
			String errMsg = e.getMessage();
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(errMsg);
			om.close();
			return result;
		}
		//数据文件列表获取
		List<OracleDtfile> dtFileList;
		try {
			dtFileList = om.getAllDtfiles();
		} catch (SQLException e) {
			String errMsg = "无法获取数据库数据文件.";
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(errMsg);
			return result;
		} finally {
			om.close();
		}
		respTime = System.currentTimeMillis() - respTime;
		if (respTime <= 0) {
			respTime = 1;
		}
		result.setResponseTime(respTime);

		for (OracleDtfile file : dtFileList) {
			MonitorResultRow row = new MonitorResultRow();
			row.setInstCode(file.getDfName());
			row.setInstName(file.getDfName());
			row.addTag("Dtfile:" + file.getDfName());
			for (int j = 0; j < ITEMIDX_LENGTH; j++) {
				row.setIndicator("ORACLE-DATAFILE-" + (j + 1), getDtValue(file, j));
			}
			result.addRow(row);
		}
		return result;
	}
}
