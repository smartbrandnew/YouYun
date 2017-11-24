package com.broada.carrier.monitor.impl.ew.exchange.basic;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.impl.common.SpeedUtil;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.carrier.monitor.spi.entity.MonitorTempData;
import com.broada.common.util.Unit;
import com.broada.component.utils.lang.SimpleProperties;

public class ExchangeBasicMonitor implements Monitor {
	public static final String ITEM_ACTIVE_CONNECTION_COUNT = "exc-basic-activeConnectionCount";
	public static final String ITEM_ACTIVE_USER_COUNT = "exc-basic-activeUserCount";
	public static final String ITEM_CLIENT_RPCS_FAILED_PS = "exc-basic-clientRPCsFailedPersec";
	public static final String ITEM_CLIENT_RPCS_SUCCEEDED_PS = "exc-basic-clientRPCsSucceededPersec";
	public static final String ITEM_MEM_CURRENT_MB_ALLOCATED = "exc-basic-exchmemCurrentMBAllocated";

	@Override
	public MonitorResult monitor(MonitorContext context) {
		MonitorTempData tempData = context.getTempData();
		if (tempData == null) 
			tempData = new MonitorTempData();
		MonitorResult mr = collect(context.getTask().getId(), new CollectContext(context), tempData);
		context.setTempData(tempData);
		return mr;
	}

	/**
	 * 传入参数srvId
	 */
	@Override
	public Serializable collect(CollectContext context) {
		MonitorTempData tempData = new MonitorTempData();
		return collect("-1", context, tempData);
	}

	private MonitorResult collect(String taskId, CollectContext context, MonitorTempData tempData) {
		CLIResult result = null;
		result = new CLIExecutor(taskId).execute(context.getNode(), context.getMethod(), "exchange-basic");

		long now = System.currentTimeMillis();

		List<Properties> items = result.getListTableResult();
		MonitorResult mr = new MonitorResult();
		if (items.size() > 0) {
			SimpleProperties props = new SimpleProperties(items.get(0));
		
			long activeConnectionCount = props.get("activeConnectionCount", 0l);
			long activeUserCount = props.get("activeUserCount", 0l);
			long clientRPCsFailed = props.get("clientRPCsFailed", 0l);
			long clientRPCsSucceeded = props.get("clientRPCsSucceeded", 0l);
			long exchmemCurrentBytesAllocated = props.get("exchmemCurrentBytesAllocated", 0l);

			Double clientRPCsFailedPS = SpeedUtil.calSpeed(tempData, ITEM_CLIENT_RPCS_FAILED_PS, clientRPCsFailed, now);
			Double clientRPCsSucceededPS = SpeedUtil.calSpeed(tempData, ITEM_CLIENT_RPCS_SUCCEEDED_PS, clientRPCsSucceeded, now);

			MonitorResultRow row = new MonitorResultRow();
			row.setIndicator(ITEM_ACTIVE_CONNECTION_COUNT, activeConnectionCount);
			row.setIndicator(ITEM_ACTIVE_USER_COUNT, activeUserCount);
			row.setIndicator(ITEM_CLIENT_RPCS_FAILED_PS, clientRPCsFailedPS);
			row.setIndicator(ITEM_CLIENT_RPCS_SUCCEEDED_PS, clientRPCsSucceededPS);
			row.setIndicator(ITEM_MEM_CURRENT_MB_ALLOCATED, Unit.B.to(Unit.MB, exchmemCurrentBytesAllocated));
			mr.addRow(row);
			
			tempData.setTime(new Date(now));
		}

		return mr;
	}
}
