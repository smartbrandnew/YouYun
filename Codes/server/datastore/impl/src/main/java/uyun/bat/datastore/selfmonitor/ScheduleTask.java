package uyun.bat.datastore.selfmonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.common.config.Config;
import uyun.bat.common.constants.MonitorConstants;
import uyun.bat.common.selfmonitor.AbstractScheduleTask;
import uyun.bat.common.selfmonitor.PerfMetricGenerator;
import uyun.bat.common.selfmonitor.PerfMetricVO;
import uyun.bat.datastore.entity.MetricSpanTime;
import uyun.bat.datastore.logic.LogicManager;

public class ScheduleTask extends AbstractScheduleTask {
	private static final Logger logger = LoggerFactory.getLogger(ScheduleTask.class);
	private long insertLength;
	private long queryLength;
	private long insertFailed;
	private long queryFailed;

	public void init() {
		if (Config.getInstance().get("selfmonitor.start", false)) {
			logger.info("Selfmonitor process start......");
			insertLength = LogicManager.getInstance().getMetricLogic().getMetricInsertAtomic();
			queryLength = LogicManager.getInstance().getMetricLogic().getMetricQueryAtomic();
			insertFailed = LogicManager.getInstance().getMetricLogic().getMetricInsertFailedAtomic();
			queryFailed = LogicManager.getInstance().getMetricLogic().getMetricQueryFaileAtomic();
			ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize);
			service.scheduleAtFixedRate(new WorkThread(oneMinutesPeriod, insertLength, queryLength), initDelay,
					oneMinutesPeriod, TimeUnit.SECONDS);
			service.scheduleAtFixedRate(new WorkThread(fiveMinutesPeriod, insertLength, queryLength), initDelay,
					fiveMinutesPeriod, TimeUnit.SECONDS);
			service
					.scheduleAtFixedRate(new WorkThread(fifteenMinutesPeriod, insertLength, queryLength), initDelay,
							fifteenMinutesPeriod, TimeUnit.SECONDS);
			service.scheduleAtFixedRate(new FaileCountThread(), initDelay, fiveMinutesPeriod, TimeUnit.SECONDS);
		}
	}

	private class WorkThread implements Runnable {
		private int period;
		private long insertLen;
		private long queryLen;

		public WorkThread(int period, long insertLen, long queryLen) {
			this.period = period;
			this.insertLen = insertLen;
			this.queryLen = queryLen;
		}

		@Override
		public void run() {
			try {
				double insertTps = 0;
				double queryTps = 0;
				synchronized (ScheduleTask.class) {
					long lastInsertLength = insertLen;
					long lastQueryLength = queryLen;
					insertLen = LogicManager.getInstance().getMetricLogic().getMetricInsertAtomic();
					queryLen = LogicManager.getInstance().getMetricLogic().getMetricQueryAtomic();
					if (insertLen > lastInsertLength) {
						insertTps = (insertLen - lastInsertLength) * 1.0 / period;
					}
					if (queryLen > lastQueryLength) {
						queryTps = (queryLen - lastQueryLength) * 1.0 / period;
					}
				}
				List<PerfMetricVO> perfMetrics = new ArrayList<PerfMetricVO>();
				List<String> tags = getTags("datastore");
				if (period == oneMinutesPeriod) {
					perfMetrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.datastore_insert_1m_tps, tags,
							insertTps));
					perfMetrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.datastore_query_1m_tps, tags,
							queryTps));
				} else if (period == fiveMinutesPeriod) {
					perfMetrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.datastore_insert_5m_tps, tags,
							insertTps));
					perfMetrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.datastore_query_5m_tps, tags,
							queryTps));
				} else if (period == fifteenMinutesPeriod) {
					perfMetrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.datastore_insert_15m_tps, tags,
							insertTps));
					perfMetrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.datastore_query_15m_tps, tags,
							queryTps));
				}
				if (perfMetrics.size() > 0)
					postPerfMetrics(perfMetrics);
			} catch (Throwable e) {
				logger.debug("Monitoring task cycle: " + period + "sec ,monitor handling capacity task exception:{}", e);
			}
		}
	}

	private class FaileCountThread implements Runnable {

		@Override
		public void run() {
			try {
				double insertFailedTps=0;
				double queryFailedTps=0;
				long lastInsertFailed = insertFailed;
				long lastQueryFailed = queryFailed;
				insertFailed = LogicManager.getInstance().getMetricLogic().getMetricInsertFailedAtomic();
				queryFailed = LogicManager.getInstance().getMetricLogic().getMetricQueryFaileAtomic();
				List<MetricSpanTime> metricSpans = LogicManager.getInstance().getResourceLogic().getMetricSpanTime();
				List<PerfMetricVO> perfMetrics = new ArrayList<PerfMetricVO>();
				for (MetricSpanTime spanTime : metricSpans) {
					List<String> tags = getTags("datastore");
					tags.add("tenant:" + spanTime.getTenantId());
					perfMetrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.metric_start_collect_time, tags,
							spanTime.getStartTime()));
					perfMetrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.metric_end_collect_time, tags,
							spanTime.getEndTime()));
					perfMetrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.metric_collect_duration_time, tags,
							spanTime.getSpanTime()));

				}
				if (insertFailed > lastInsertFailed) {
					insertFailedTps = (insertFailed - lastInsertFailed)*1.0/fifteenMinutesPeriod ;
				}
				if (queryFailed > lastQueryFailed) {
					queryFailedTps = (queryFailed - lastQueryFailed)*1.0/fifteenMinutesPeriod;
				}
				List<String> tags = getTags("datastore");
				perfMetrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.datastore_insert_5m_failed_count, tags,
						insertFailedTps));
				perfMetrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.datastore_query_5m_failed_count, tags,
						queryFailedTps));
				if (perfMetrics.size() > 0)
					postPerfMetrics(perfMetrics);
			} catch (Throwable e) {
				logger.debug("Monitoring period:5min ,monitor handling capacity task exception:{}", e);
			}
		}
	}
}
