package com.broada.carrier.monitor.impl.ew.exchange.pop3;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.carrier.monitor.spi.entity.MonitorTempData;
import com.broada.component.utils.lang.SimpleProperties;

/**
 * Exchange2010的POP3监测器实现
 * Company: Broada
 * @author zhaoyn
 * @version 1.0
 */
public class ExchangePop3Monitor implements Monitor {
	public static final String ITEM_CONNECTIONS_CURRENT = "exc-connections-current";
	public static final String ITEM_CONNECTIONS_FAILED = "exc-connections-failed";
	public static final String ITEM_CONNECTIONS_REJECTED = "exc-connections-rejected";
	public static final String ITEM_CONNECTIONS_TOTAL = "exc-connections-total";

	@Override
	public MonitorResult monitor(MonitorContext context) {
		MonitorTempData tempData = context.getTempData();
		if (tempData == null)
			tempData = new MonitorTempData();
		MonitorResult mr = collect(context.getTask().getId(), new CollectContext(context), tempData);
		context.setTempData(tempData);
		return mr;
	}

	@Override
	public Serializable collect(CollectContext context) {
		MonitorTempData tempData = new MonitorTempData();
		return collect("-1", context, tempData);
	}

	private MonitorResult collect(String taskId, CollectContext context, MonitorTempData tempData) {
		CLIResult result = null;
		result = new CLIExecutor(taskId).execute(context.getNode(), context.getMethod(), "exchange2010-pop3");
		long now = System.currentTimeMillis();

		List<Properties> items = result.getListTableResult();
		MonitorResult mr = new MonitorResult();
		for (int index = 0; index < items.size(); index++) {
			SimpleProperties props = new SimpleProperties(items.get(index));
			String name = props.get("name");
			if (!"_total".equals(name.trim()) && items.size() > 1)
				continue;
			Long connectionsCurrent = props.get("connectionsCurrent", 0l);
			Long connectionsFailed = props.get("connectionsFailed", 0l);
			Long connectionsRejected = props.get("connectionsRejected", 0l);
			Long connectionsTotal = props.get("connectionsTotal", 0l);

			MonitorResultRow row = new MonitorResultRow();
			row.setIndicator(ITEM_CONNECTIONS_CURRENT, connectionsCurrent);
			row.setIndicator(ITEM_CONNECTIONS_FAILED, connectionsFailed);
			row.setIndicator(ITEM_CONNECTIONS_REJECTED, connectionsRejected);
			row.setIndicator(ITEM_CONNECTIONS_TOTAL, connectionsTotal);

			mr.addRow(row);
		}
		tempData.setTime(new Date(now));
		return mr;
	}
}
