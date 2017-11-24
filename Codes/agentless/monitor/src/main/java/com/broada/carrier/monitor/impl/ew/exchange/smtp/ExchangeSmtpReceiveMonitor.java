package com.broada.carrier.monitor.impl.ew.exchange.smtp;

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
 * Exchange2010的SmtpReceive监测器实现 Company: Broada
 * 
 * @author zhaoyn
 * @version 1.0
 */
public class ExchangeSmtpReceiveMonitor implements Monitor {
	public static final String ITEM_BYTES_RECEIVED_PS = "exc-bytes-ReceivedPersec";
	public static final String ITEM_BYTES_RECEIVED_TOTAL = "exc-bytes-ReceivedTotal";
	public static final String ITEM_MESSAGE_BYTES_RECEIVED_PS = "exc-messageBytes-ReceivedPersec";
	public static final String ITEM_MESSAGE_BYTES_RECEIVED_TOTAL = "exc-messageBytes-ReceivedTotal";
	public static final String ITEM_MESSAGES_RECEIVED_PS = "exc-messages-ReceivedPersec";
	public static final String ITEM_MESSAGES_RECEIVED_TOTAL = "exc-messages-ReceivedTotal";

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
		result = new CLIExecutor(taskId).execute(context.getNode(), context.getMethod(), "exchange2010-smtpreceived");
		long now = System.currentTimeMillis();

		List<Properties> items = result.getListTableResult();
		MonitorResult mr = new MonitorResult();
		for (int index = 0; index < items.size(); index++) {
			SimpleProperties props = new SimpleProperties(items.get(index));
			String name = props.get("name");
			if (!"_total".equals(name.trim()) && items.size() > 1)
				continue;
			Long bytesReceivedPersec = props.get("bytesReceivedPersec", 0l);
			Long bytesReceivedTotal = props.get("bytesReceivedTotal", 0l) / 1024;
			Long messageBytesReceivedPersec = props.get("messageBytesReceivedPersec", 0l);
			Long messageBytesReceivedTotal = props.get("messageBytesReceivedTotal", 0l) / 1024;
			Long messagesReceivedPersec = props.get("messagesReceivedPersec", 0l);
			Long messagesReceivedTotal = props.get("messagesReceivedTotal", 0l);

			MonitorResultRow row = new MonitorResultRow();
			row.setIndicator(ITEM_BYTES_RECEIVED_PS, bytesReceivedPersec);
			row.setIndicator(ITEM_BYTES_RECEIVED_TOTAL, bytesReceivedTotal);
			row.setIndicator(ITEM_MESSAGE_BYTES_RECEIVED_PS, messageBytesReceivedPersec);
			row.setIndicator(ITEM_MESSAGE_BYTES_RECEIVED_TOTAL, messageBytesReceivedTotal);
			row.setIndicator(ITEM_MESSAGES_RECEIVED_PS, messagesReceivedPersec);
			row.setIndicator(ITEM_MESSAGES_RECEIVED_TOTAL, messagesReceivedTotal);

			mr.addRow(row);
		}
		tempData.setTime(new Date(now));
		return mr;
	}
}
