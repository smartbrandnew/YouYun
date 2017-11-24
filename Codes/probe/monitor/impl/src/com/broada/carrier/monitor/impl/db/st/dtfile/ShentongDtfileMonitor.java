package com.broada.carrier.monitor.impl.db.st.dtfile;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.st.ShentongManager;
import com.broada.carrier.monitor.method.st.ShentongMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * Shentong 数据文件监测实现
 *  文件名.列名作为key的方式可行性确认
 * 
 * @author Zhouqa
 * Create By 2016年4月13日 上午9:42:32
 */
public class ShentongDtfileMonitor implements Monitor {

	private static final Log logger = LogFactory.getLog(ShentongDtfileMonitor.class);
	public static final String SEPARATOR = "\u007F";

	private static final int ITEMIDX_LENGTH = 8;

	/**
	 * 取得index对应CLOUMNS引索列对应的监测结果值
	 * @param dtFileList
	 * @param dtFileName
	 * @param index
	 * @return
	 */
	private Double getDtValue(ShentongDtfile dtFile, int index) {
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

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setMessage("采集指标开始...");
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		ShentongMethod option = new ShentongMethod(context.getMethod());
		ShentongManager sm = new ShentongManager(context.getNode().getIp(), option);
		long respTime = System.currentTimeMillis();
		try {
			sm.initConnection();
			result.setMessage("采集指标进行中....");
		} catch (ClassNotFoundException e) {
			result.setState(MonitorState.FAILED);
			result.setResultDesc(e.getMessage());
			respTime = System.currentTimeMillis() - respTime;
			if (respTime <= 0) {
				respTime = 1;
			}
			result.setResponseTime(respTime);
			sm.close();
			return result;
		} catch (SQLException e) {
			String errMsg = e.getMessage();
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(MonitorState.FAILED);
			result.setResultDesc(errMsg);
			sm.close();
			return result;
		} 
		//数据文件列表获取
		List<ShentongDtfile> dtFileList;
		try {
			dtFileList = sm.getAllDtfiles();
		} catch (SQLException e) {
			String errMsg = "无法获取数据库数据文件." + e;
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(MonitorState.FAILED);
			result.setResultDesc(errMsg);
			return result;
		} finally {
			sm.close();
		}
		respTime = System.currentTimeMillis() - respTime;
		if (respTime <= 0) {
			respTime = 1;
		}
		result.setResponseTime(respTime);

		for (ShentongDtfile file : dtFileList) {
			MonitorResultRow row = new MonitorResultRow();
			row.setInstCode(file.getDfName());
			row.setInstName(file.getDfName());
			result.addRow(row);
		}
		result.setMessage("采集工作完成。");
		result.setState(MonitorState.SUCCESSED);
		return result;
	}

	@Override
	public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);

		ShentongMethod option = new ShentongMethod(context.getMethod());
		ShentongManager sm = new ShentongManager(context.getNode().getIp(), option);
		long respTime = System.currentTimeMillis();
		try {
			sm.initConnection();
		} catch (ClassNotFoundException lde) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(lde.getMessage());
			respTime = System.currentTimeMillis() - respTime;
			if (respTime <= 0) {
				respTime = 1;
			}
			result.setResponseTime(respTime);
			sm.close();
			return result;
		} catch (SQLException e) {
			String errMsg = e.getMessage();
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(errMsg);
			sm.close();
			return result;
		}
		//数据文件列表获取
		List<ShentongDtfile> dtFileList;
		try {
			dtFileList = sm.getAllDtfiles();
		} catch (SQLException e) {
			String errMsg = "无法获取数据库数据文件." + e;
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(errMsg);
			return result;
		} finally {
			sm.close();
		}
		respTime = System.currentTimeMillis() - respTime;
		if (respTime <= 0) {
			respTime = 1;
		}
		result.setResponseTime(respTime);

		for (ShentongDtfile file : dtFileList) {
			MonitorResultRow row = new MonitorResultRow();
			row.setInstCode(file.getDfName());
			row.setInstName(file.getDfName());

			for (int j = 0; j < ITEMIDX_LENGTH; j++) {
				row.setIndicator("SHENTONG-DATAFILE-" + (j + 1), getDtValue(file, j));
			}
			result.addRow(row);
		}
		return result;
	}
}
