package com.broada.carrier.monitor.impl.ew.domino.perf;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.ew.domino.common.*;
import com.broada.carrier.monitor.method.domino.DominoMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DominoPerfMonitor extends BaseMonitor {
	static final String[] PERFNAMES = new String[] { "Server.Version.Notes", "Server.Trans.PerMinute",
			"Server.Trans.PerMinute.Peak", "Server.Trans.Total", "Server.Users", "Server.Users.Peak", "Server.Tasks",
			"Server.Path.Data", "Replica.Successful", "Replica.Failed", "Replica.Docs.Deleted", "Replica.Docs.Added",
			"Replica.Docs.Updated", "Server.CPU.Count", "MAIL.Dead", "MAIL.TotalRouted", "MAIL.Waiting", "MAIL.Delivered",
			"MAIL.WaitingRecipients", "MAIL.AverageSizeDelivered ", "MAIL.MaximumSizeDelivered", "Server.CPU.Type",
			"Database.BufferPool.Maximum", "Database.BufferPool.Used", "Database.BufferPool.Peak", "Database.ExtMgrPool.Used",
			"Database.ExtMgrPool.Peak", "Database.NSFPool.Used", "Database.NSFPool.Peak", "Mail.AverageDeliverTime",
			"Mail.AverageServerHops", "Mail.TotalFailures", "Disc.c.Free", "Disc.c.Size" };
	static final String[] PERFS_ITEM_CODE = new String[] { "DOMINO_PERF-1", "DOMINO_PERF-10", "DOMINO_PERF-11",
			"DOMINO_PERF-12", "DOMINO_PERF-13", "DOMINO_PERF-14", "DOMINO_PERF-15", "DOMINO_PERF-2", "DOMINO_PERF-20",
			"DOMINO_PERF-21", "DOMINO_PERF-22", "DOMINO_PERF-23", "DOMINO_PERF-24", "DOMINO_PERF-3", "DOMINO_PERF-30",
			"DOMINO_PERF-31", "DOMINO_PERF-32", "DOMINO_PERF-33", "DOMINO_PERF-34", "DOMINO_PERF-35", "DOMINO_PERF-36",
			"DOMINO_PERF-4", "DOMINO_PERF-40", "DOMINO_PERF-41", "DOMINO_PERF-42", "DOMINO_PERF-43", "DOMINO_PERF-44",
			"DOMINO_PERF-45", "DOMINO_PERF-46", "DOMINO_PERF-50", "DOMINO_PERF-51", "DOMINO_PERF-52", "DOMINO_PERF-53",
			"DOMINO_PERF-54", "DOMINO_PERF-55" };
	static final int[] PERFS_ITEM_TYPE = new int[] { ValueType.STRING, ValueType.DOUBLE,
			ValueType.DOUBLE, ValueType.DOUBLE, ValueType.DOUBLE, ValueType.DOUBLE,
			ValueType.DOUBLE, ValueType.STRING, ValueType.DOUBLE, ValueType.DOUBLE,
			ValueType.DOUBLE, ValueType.DOUBLE, ValueType.DOUBLE, ValueType.DOUBLE,
			ValueType.DOUBLE, ValueType.DOUBLE, ValueType.DOUBLE, ValueType.DOUBLE,
			ValueType.DOUBLE, ValueType.DOUBLE, ValueType.DOUBLE, ValueType.STRING,
			ValueType.DOUBLE, ValueType.DOUBLE, ValueType.DOUBLE, ValueType.DOUBLE,
			ValueType.DOUBLE, ValueType.DOUBLE, ValueType.DOUBLE, ValueType.DOUBLE,
			ValueType.DOUBLE, ValueType.DOUBLE, ValueType.DOUBLE, ValueType.DOUBLE,
			ValueType.DOUBLE };

	static final String R5_STAT_VIEW = "1. Statistics Reports \\ System";

	static final String R6_STAT_VIEW = "Statistics Reports \\ System";

	static final String R7_STAT_VIEW = "Statistics Reports \\ System";

	static final String STAT_DB = "statrep.nsf";

	@Override public Serializable collect(CollectContext context) {
		DominoTemplate domino = null;
		try {
			DominoMonitorMethodOption options = new DominoMonitorMethodOption(context.getMethod());
			domino = DominoTemplate
					.getInstance(context.getNode().getIp(), options.getUsername(), options.getPassword(), options.getPort());
			String view = R5_STAT_VIEW;
			if (domino.getSession().getNotesVersion().indexOf("Release 5") != -1)
				view = R5_STAT_VIEW;
			else if (domino.getSession().getNotesVersion().indexOf("Release 6") != -1)
				view = R6_STAT_VIEW;
			else
				view = R7_STAT_VIEW;
			List<PerfResult> perfs = new ArrayList<PerfResult>();
			Object[] vs = domino.getFirstDocValues(STAT_DB, view, PERFNAMES);
			int i = 0;
			for (int j = 0; j < PERFS_ITEM_CODE.length; j++) {
				i = domino.constructMonitorItem(PERFS_ITEM_CODE[j], perfs, i, vs, PERFS_ITEM_TYPE[j]);
			}

			String instKey = context.getNode().getIp() + ":" + options.getPort() + ":" + options.getUsername() + ":" + options
					.getVersion();
			for (PerfResult perf : perfs) {
				perf.setInstanceKey(instKey);
			}
			MonitorInstance mi = new MonitorInstance();
			mi.setInstanceKey(instKey);
			mi.setInstanceName("Domain服务器[" + context.getNode().getIp() + "]");
			MonitorResult result = new MonitorResult();
			result.setPerfResults(perfs.toArray(new PerfResult[perfs.size()]));
			return result;
		} catch (Exception e) {
			throw new CollectException(e);
		} finally {
			if (domino != null)
				domino.recycle();
		}
	}
}
