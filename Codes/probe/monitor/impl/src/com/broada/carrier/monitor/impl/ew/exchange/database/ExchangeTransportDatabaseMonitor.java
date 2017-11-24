package com.broada.carrier.monitor.impl.ew.exchange.database;

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
 * Exchange2010的database监测器实现
 * Company: Broada
 * @author zhaoyn
 * @version 1.0
 */
public class ExchangeTransportDatabaseMonitor implements Monitor {
	public static final String ITEM_STREAM_BYTES_READ_PS = "exc-stream-bytes-readPersec";
	public static final String ITEM_STREAM_BYTES_READ_TOTAL = "exc-stream-bytes-readTotal";
	public static final String ITEM_STREAM_BYTES_WRITTEN_PS = "exc-stream-bytes-writtenPersec";
	public static final String ITEM_STREAM_BYTES_WRITTEN_TOTAL = "exc-stream-bytes-writtenTotal";
	public static final String ITEM_STREAM_READ_PS = "exc-stream-readPersec";
	public static final String ITEM_STREAM_READ_TOTAL = "exc-stream-readTotal";
	public static final String ITEM_STREAM_WRITES_PS = "exc-stream-writesPersec";
	public static final String ITEM_STREAM_WRITES_TOTAL = "exc-stream-writesTotal";

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
		result = new CLIExecutor(taskId).execute(context.getNode(), context.getMethod(), "exchange2010-database");
		long now = System.currentTimeMillis();

		List<Properties> items = result.getListTableResult();
		MonitorResult mr = new MonitorResult();
		for (int index = 0; index < items.size(); index++) {
			SimpleProperties props = new SimpleProperties(items.get(index));
			String name = props.get("name");
			if (!"_total".equals(name.trim()) && items.size() > 1)
				continue;
			Long streambytesreadPersec = props.get("streambytesreadPersec", 0l);
			Long streambytesreadtotal = props.get("streambytesreadtotal", 0l) / 1024;
			Long streambyteswrittenPersec = props.get("streambyteswrittenPersec", 0l);
			Long streambyteswrittentotal = props.get("streambyteswrittentotal", 0l) / 1024;
			Long streamreadPersec = props.get("streamreadPersec", 0l);
			Long streamreadtotal = props.get("streamreadtotal", 0l);
			Long streamwritesPersec = props.get("streamwritesPersec", 0l);
			Long streamwritestotal = props.get("streamwritestotal", 0l);

			MonitorResultRow row = new MonitorResultRow();
			row.setIndicator(ITEM_STREAM_BYTES_READ_PS, streambytesreadPersec);
			row.setIndicator(ITEM_STREAM_BYTES_READ_TOTAL, streambytesreadtotal);
			row.setIndicator(ITEM_STREAM_BYTES_WRITTEN_PS, streambyteswrittenPersec);
			row.setIndicator(ITEM_STREAM_BYTES_WRITTEN_TOTAL, streambyteswrittentotal);
			row.setIndicator(ITEM_STREAM_READ_PS, streamreadPersec);
			row.setIndicator(ITEM_STREAM_READ_TOTAL, streamreadtotal);
			row.setIndicator(ITEM_STREAM_WRITES_PS, streamwritesPersec);
			row.setIndicator(ITEM_STREAM_WRITES_TOTAL, streamwritestotal);

			mr.addRow(row);
		}
		tempData.setTime(new Date(now));
		return mr;
	}
}
