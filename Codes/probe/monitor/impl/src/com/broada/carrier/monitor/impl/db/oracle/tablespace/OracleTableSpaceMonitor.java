package com.broada.carrier.monitor.impl.db.oracle.tablespace;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import com.broada.carrier.monitor.impl.db.oracle.util.OracleErrorUtil;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleManager;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.CollectMonitorState;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;

import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * <p>
 * Title: OracleTableSpaceMonitor
 * </p>
 * <p>
 * Description: COSS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author plx
 * @version 2.3
 */

public class OracleTableSpaceMonitor implements Monitor {
	public static final MonitorItem[] ITEMS = new MonitorItem[] {

	};

	private void setResult(MonitorResultRow row, int index, String value) {
		if (value != null)
			row.setIndicator("ORACLE-TABLESPACE-" + index, value);
	}

	private void setResult(MonitorResultRow row, int index, double value) {
		if (value >= 0)
			row.setIndicator("ORACLE-TABLESPACE-" + index, value);
	}

	private CollectResult fillCollectResultFromModels(List<OracleTableSpace> otss, CollectResult result) {
		for (int i = 0; i < otss.size(); i++) {
			OracleTableSpace ots = otss.get(i);
			MonitorResultRow row = new MonitorResultRow(ots.getTsName());
			setResult(row, 1, ots.getUsed());
			setResult(row, 2, ots.getUsedPct());
			setResult(row, 3, ots.getMaxExtents());
			setResult(row, 4, ots.getAvgReadTim());
			setResult(row, 5, ots.getAvgWriteTim());
			setResult(row, 6, ots.getExtentCount());
			setResult(row, 7, ots.getNextExtent());
			setResult(row, 8, ots.getFreeExtents());
			setResult(row, 9, ots.getSegmentManagementType());
			setResult(row, 10, ots.getSpaceType());
			setResult(row, 11, ots.getFree());
			setResult(row, 12, ots.getCurAvailTS());
			setResult(row, 13, ots.getMaxSpace());
			setResult(row, 14, ots.getAutoExtend());
			result.addRow(row);
			result.setProgress((i + 1) * 100 / otss.size());
			result.setMessage("采集表空间： " + ots.getTsName() + "完成。");

		}
		result.setMessage("采集工作完成。");
		result.setProgress(100);
		result.setState(CollectMonitorState.SUCCESSED);
		return result;
	}

	private MonitorResult fillCollectResultFromModels(List<OracleTableSpace> otss) {
		MonitorResult result = new MonitorResult();
		for (int i = 0; i < otss.size(); i++) {
			OracleTableSpace ots = otss.get(i);
			MonitorResultRow row = new MonitorResultRow(ots.getTsName());
			row.addTag("tablespaceName:" + ots.getTsName());
			setResult(row, 1, ots.getUsed());
			setResult(row, 2, ots.getUsedPct());
			setResult(row, 3, ots.getMaxExtents());
			setResult(row, 4, ots.getAvgReadTim());
			setResult(row, 5, ots.getAvgWriteTim());
			setResult(row, 6, ots.getExtentCount());
			setResult(row, 7, ots.getNextExtent());
			setResult(row, 8, ots.getFreeExtents());
			setResult(row, 9, ots.getSegmentManagementType());
			setResult(row, 10, ots.getSpaceType());
			setResult(row, 11, ots.getFree());
			setResult(row, 12, ots.getCurAvailTS());
			setResult(row, 13, ots.getMaxSpace());
			setResult(row, 14, ots.getAutoExtend());
			result.addRow(row);
		}
		return result;
	}

	@Override
	public Serializable collect(CollectContext context) {
		CollectResult result = context.getResult();
		result.setProgress(1);
		result.setMessage("采集指标开始...");
		result.setState(CollectMonitorState.START);
		OracleMethod oracleMethod = new OracleMethod(context.getMethod());
		OracleManager om = new OracleManager(context.getNode().getIp(), oracleMethod);
		try {
			om.initConnection();
			result.setMessage("采集指标进行中....");
			result.setState(CollectMonitorState.PROCESSING);
			List<OracleTableSpace> tSpaces = om.getAllTableSpaces();
			fillCollectResultFromModels(tSpaces, result);
			return result;
		} catch (SQLException e) {
			result.setMessage("采集失败，发生异常: " + e);
			throw OracleErrorUtil.createError(e);
		} finally {
			om.close();
		}
	}

	@Override
	public MonitorResult monitor(MonitorContext context) {
		OracleMethod oracleMethod = new OracleMethod(context.getMethod());
		OracleManager om = new OracleManager(context.getNode().getIp(), oracleMethod);
		try {
			om.initConnection();
			List<OracleTableSpace> tSpaces = om.getAllTableSpaces();
			return fillCollectResultFromModels(tSpaces);
		} catch (SQLException e) {
			throw OracleErrorUtil.createError(e);
		} finally {
			om.close();
		}
	}

}
