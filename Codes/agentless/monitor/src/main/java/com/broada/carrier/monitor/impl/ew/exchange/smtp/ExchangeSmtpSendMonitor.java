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
 * Exchange2010的SmtpSend监测器实现
 * Company: Broada
 * @author zhaoyn
 * @version 1.0
 */
public class ExchangeSmtpSendMonitor implements Monitor {
	public static final String ITEM_BYTES_SENT_PS = "exc-bytes-SentPersec";
	public static final String ITEM_BYTES_SENT_TOTAL = "exc-bytes-SentTotal";
	public static final String ITEM_MESSAGE_BYTES_SENT_PS = "exc-messageBytes-SentPersec";
	public static final String ITEM_MESSAGE_BYTES_SENT_TOTAL = "exc-messageBytes-SentTotal";
	public static final String ITEM_MESSAGES_SENT_PS = "exc-messages-SentPersec";
	public static final String ITEM_MESSAGES_SENT_TOTAL = "exc-messages-SentTotal";

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
		result = new CLIExecutor(taskId).execute(context.getNode(), context.getMethod(), "exchange2010-smtpsent");
		long now = System.currentTimeMillis();

		List<Properties> items = result.getListTableResult();
		MonitorResult mr = new MonitorResult();
		for (int index = 0; index < items.size(); index++) {
			SimpleProperties props = new SimpleProperties(items.get(index));
			String name = props.get("name");
			if (!"_total".equals(name.trim()) && items.size() > 1)
				continue;
			Long bytesSentPersec = props.get("bytesSentPersec", 0l);
			Long bytesSentTotal = props.get("bytesSentTotal", 0l) / 1024;
			Long messageBytesSentPersec = props.get("messageBytesSentPersec", 0l);
			Long messageBytesSentTotal = props.get("messageBytesSentTotal", 0l) / 1024;
			Long messagesSentPersec = props.get("messagesSentPersec", 0l);
			Long messagesSentTotal = props.get("messagesSentTotal", 0l);

			MonitorResultRow row = new MonitorResultRow();
			row.setIndicator(ITEM_BYTES_SENT_PS, bytesSentPersec);
			row.setIndicator(ITEM_BYTES_SENT_TOTAL, bytesSentTotal);
			row.setIndicator(ITEM_MESSAGE_BYTES_SENT_PS, messageBytesSentPersec);
			row.setIndicator(ITEM_MESSAGE_BYTES_SENT_TOTAL, messageBytesSentTotal);
			row.setIndicator(ITEM_MESSAGES_SENT_PS, messagesSentPersec);
			row.setIndicator(ITEM_MESSAGES_SENT_TOTAL, messagesSentTotal);

			mr.addRow(row);
		}
		tempData.setTime(new Date(now));
		return mr;
	}
}
