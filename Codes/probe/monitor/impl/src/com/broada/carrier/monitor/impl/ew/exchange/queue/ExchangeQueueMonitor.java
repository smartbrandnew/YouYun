package com.broada.carrier.monitor.impl.ew.exchange.queue;

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
 * Exchange2010的Queue监测器实现
 * Company: Broada
 * @author zhaoyn
 * @version 1.0
 */
public class ExchangeQueueMonitor implements Monitor {
	public static final String ITEM_ACTIVE_MAILBOX_DELIVERY_QUEUE_LENGTH = "exc-active-mailbox-delivery-QueueLength";
	public static final String ITEM_MESSAGES_QUEUED_FOR_DELIVERY_PS = "exc-messages-queued-forDelivery-PS";
	public static final String ITEM_MESSAGES_QUEUED_FOR_DELIVERY_TOTAL = "exc-messages-queued-forDelivery-total";
	public static final String ITEM_MESSAGES_SUBMITTED_PS = "exc-messages-SubmittedPersec";
	public static final String ITEM_MESSAGES_SUBMITTED_TOTAL = "exc-messages-SubmittedTotal";
	public static final String ITEM_SUBMISSION_QUEUE_LENGTH = "exc-submission-QueueLength";

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
		result = new CLIExecutor(taskId).execute(context.getNode(), context.getMethod(), "exchange2010-queue");
		long now = System.currentTimeMillis();

		List<Properties> items = result.getListTableResult();
		MonitorResult mr = new MonitorResult();
		for (int index = 0; index < items.size(); index++) {
			SimpleProperties props = new SimpleProperties(items.get(index));
			String name = props.get("name");
			if (!"_total".equals(name.trim()) && items.size() > 1)
				continue;
			Long activeMailboxDeliveryQueuelength = props.get("activeMailboxDeliveryQueuelength", 0l);
			Long messagesQueuedforDeliveryPerSecond = props.get("messagesQueuedforDeliveryPerSecond", 0l);
			Long messagesQueuedforDeliveryTotal = props.get("messagesQueuedforDeliveryTotal", 0l);
			Long messagesSubmittedPersecond = props.get("messagesSubmittedPersecond", 0l);
			Long messagesSubmittedTotal = props.get("messagesSubmittedTotal", 0l);
			Long submissionQueueLength = props.get("submissionQueueLength", 0l);

			MonitorResultRow row = new MonitorResultRow();
			row.setIndicator(ITEM_ACTIVE_MAILBOX_DELIVERY_QUEUE_LENGTH, activeMailboxDeliveryQueuelength);
			row.setIndicator(ITEM_MESSAGES_QUEUED_FOR_DELIVERY_PS, messagesQueuedforDeliveryPerSecond);
			row.setIndicator(ITEM_MESSAGES_QUEUED_FOR_DELIVERY_TOTAL, messagesQueuedforDeliveryTotal);
			row.setIndicator(ITEM_MESSAGES_SUBMITTED_PS, messagesSubmittedPersecond);
			row.setIndicator(ITEM_MESSAGES_SUBMITTED_TOTAL, messagesSubmittedTotal);
			row.setIndicator(ITEM_SUBMISSION_QUEUE_LENGTH, submissionQueueLength);

			mr.addRow(row);
		}
		tempData.setTime(new Date(now));
		return mr;
	}
}
