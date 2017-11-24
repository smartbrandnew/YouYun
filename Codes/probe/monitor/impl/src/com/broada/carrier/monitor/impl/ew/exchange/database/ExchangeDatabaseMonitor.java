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

public class ExchangeDatabaseMonitor implements Monitor {
	public static final String ITEM_DATABASE_CACHE_SIZE = "exc-database-size";
	public static final String ITEM_IO_DATABASE_READS_PS = "exc-io-database-readsPersec";
	public static final String ITEM_IO_DATABASE_WRITES_PS = "exc-io-database-writesPersec";
	public static final String ITEM_IO_LOG_READS_PS = "exc-io-log-readsPersec";
	public static final String ITEM_IO_LOG_WRITES_PS = "exc-io-log-writesPersec";
	public static final String ITEM_IO_BYTES_WRITES_PS = "exc-io-bytes-writesPersec";

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
		result = new CLIExecutor(taskId).execute(context.getNode(), context.getMethod(), "exchange-database");
		long now = System.currentTimeMillis();
		List<Properties> items = result.getListTableResult();
		MonitorResult mr = new MonitorResult();

		for (int index = 0; index < items.size(); index++) {
			SimpleProperties props = new SimpleProperties(items.get(index));
			String name = props.get("name");
			if (!"_total".equals(name.trim()) && items.size() > 1)
				continue;
			long databaseCacheSizeMB = props.get("databaseCacheSizeMB", 01);
			long ioDatabaseReadsPersec = props.get("ioDatabaseReadsPersec", 01);
			long ioDatabaseWritesPersec = props.get("ioDatabaseWritesPersec", 01);
			long ioLogReadsPersec = props.get("ioLogReadsPersec", 01);
			long ioLogWritesPersec = props.get("ioLogWritesPersec", 01);
			long logBytesWritePersec = props.get("logBytesWritePersec", 01);
			MonitorResultRow row = new MonitorResultRow();
			row.setIndicator(ITEM_DATABASE_CACHE_SIZE, databaseCacheSizeMB);
			row.setIndicator(ITEM_IO_DATABASE_READS_PS, ioDatabaseReadsPersec);
			row.setIndicator(ITEM_IO_DATABASE_WRITES_PS, ioDatabaseWritesPersec);
			row.setIndicator(ITEM_IO_LOG_READS_PS, ioLogReadsPersec);
			row.setIndicator(ITEM_IO_LOG_WRITES_PS, ioLogWritesPersec);
			row.setIndicator(ITEM_IO_BYTES_WRITES_PS, logBytesWritePersec);
			mr.addRow(row);
		}
		tempData.setTime(new Date(now));
		return mr;
	}

}
