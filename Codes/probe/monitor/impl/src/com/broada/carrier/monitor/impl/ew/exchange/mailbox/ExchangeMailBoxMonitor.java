package com.broada.carrier.monitor.impl.ew.exchange.mailbox;

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
import com.broada.component.utils.lang.SimpleProperties;

public class ExchangeMailBoxMonitor implements Monitor {
	public static final String ITEM_MESSAGES_DELIVERED_PS = "exc-mailbox-messagesDeliveredPersec";
	public static final String ITEM_MESSAGES_SENT_PS = "exc-mailbox-messagesSentPersec";
	public static final String ITEM_MESSAGES_SUBMITTED_PS = "exc-mailbox-messagesSubmittedPersec";
	public static final String ITEM_RECEIVE_SIZE = "exc-mailbox-receiveSize";

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
		result = new CLIExecutor(taskId).execute(context.getNode(), context.getMethod(), "exchange-mailbox");
		long now = System.currentTimeMillis();

		List<Properties> items = result.getListTableResult();
		MonitorResult mr = new MonitorResult();
		for (int index = 0; index < items.size(); index++) {
			SimpleProperties props = new SimpleProperties(items.get(index));
			String name = props.get("name");
			if (!"_total".equals(name.trim()) && items.size() > 1)
				continue;
			long messagesDelivered = props.get("messagesDelivered", 01);
			long messagesSent = props.get("messagesSent", 01);
			long messagesSubmitted = props.get("messagesSubmitted", 01);
			long receiveQueueSize = props.get("receiveQueueSize", 01);

			Double messagesDeliveredPS = SpeedUtil.calSpeed(tempData, ITEM_MESSAGES_DELIVERED_PS, messagesDelivered, now);
			Double messagesSentPS = SpeedUtil.calSpeed(tempData, ITEM_MESSAGES_SENT_PS, messagesSent, now);
			Double messagesSubmittedPS = SpeedUtil.calSpeed(tempData, ITEM_MESSAGES_SUBMITTED_PS, messagesSubmitted, now);

			MonitorResultRow row = new MonitorResultRow();
			row.setIndicator(ITEM_MESSAGES_DELIVERED_PS, messagesDeliveredPS);
			row.setIndicator(ITEM_MESSAGES_SENT_PS, messagesSentPS);
			row.setIndicator(ITEM_MESSAGES_SUBMITTED_PS, messagesSubmittedPS);
			row.setIndicator(ITEM_RECEIVE_SIZE, receiveQueueSize);

			mr.addRow(row);
		}
		tempData.setTime(new Date(now));
		return mr;
	}
}
