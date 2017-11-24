package uyun.bat.event.impl.selfmonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.common.config.Config;
import uyun.bat.common.constants.MonitorConstants;
import uyun.bat.common.selfmonitor.AbstractScheduleTask;
import uyun.bat.common.selfmonitor.PerfMetricGenerator;
import uyun.bat.common.selfmonitor.PerfMetricVO;
import uyun.bat.event.api.entity.EventSpanTime;
import uyun.bat.event.api.logic.EventLogic;

public class ScheduleTask extends AbstractScheduleTask {
	private static final Logger logger = LoggerFactory.getLogger(ScheduleTask.class);
	private long insertLength;
	private long eventPageLength;
	private long eventGraphLength;
	private long insertFailed;
	private long queryFailed;
	@Autowired
	private EventLogic eventLogic;

	public void init() {
		if (Config.getInstance().get("selfmonitor.start", false)) {
			logger.info("Slefmonitor process start......");
			insertLength = eventLogic.getInsertAtomic();
			eventPageLength = eventLogic.getEventPageAtomic();
			eventGraphLength = eventLogic.getEventGraphAtomic();
			insertFailed = eventLogic.getInsertFailedAtomic();
			queryFailed = eventLogic.getQueryFailedAtomic();
			ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize);
			service.scheduleAtFixedRate(new WorkThread(oneMinutesPeriod, insertLength, eventPageLength, eventGraphLength),
					initDelay, oneMinutesPeriod, TimeUnit.SECONDS);
			service.scheduleAtFixedRate(new WorkThread(fiveMinutesPeriod, insertLength, eventPageLength, eventGraphLength),
					initDelay, fiveMinutesPeriod, TimeUnit.SECONDS);
			service
					.scheduleAtFixedRate(new WorkThread(fifteenMinutesPeriod, insertLength, eventPageLength, eventGraphLength),
							initDelay, fifteenMinutesPeriod, TimeUnit.SECONDS);
			service.scheduleAtFixedRate(new FaileCountThread(), initDelay, fiveMinutesPeriod, TimeUnit.SECONDS);
		}
	}

	private class WorkThread implements Runnable {
		private int period;
		private long insertLen;
		private long eventPageLen;
		private long eventGraphLen;

		public WorkThread(int period, long insertLen, long eventPageLen, long eventGraphLen) {
			this.period = period;
			this.insertLen = insertLen;
			this.eventPageLen = eventPageLen;
			this.eventGraphLen = eventGraphLen;
		}

		@Override
		public void run() {
			try {
				double insertTps = 0;
				double queryPageTps = 0;
				double queryGraphTps = 0;
				long lastInsertLength = insertLen;
				long lastEventPageLength = eventPageLen;
				long lastEventGraphLength = eventGraphLen;
				insertLen = eventLogic.getInsertAtomic();
				eventPageLen = eventLogic.getEventPageAtomic();
				eventGraphLen = eventLogic.getEventGraphAtomic();
				if (insertLen > lastInsertLength) {
					insertTps = (insertLen - lastInsertLength) * 1.0 / period;
				}
				if (eventPageLen > lastEventPageLength) {
					queryPageTps = (eventPageLen - lastEventPageLength) * 1.0 / period;
				}
				if (eventGraphLen > lastEventGraphLength) {
					queryGraphTps = (eventGraphLen - lastEventGraphLength) * 1.0 / period;
				}

				List<PerfMetricVO> metrics = new ArrayList<PerfMetricVO>();
				List<String> tags = getTags("event");

				if (period == oneMinutesPeriod) {
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.event_insert_1m_tps, tags, insertTps));
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.event_query_page_1m_tps, tags,
							queryPageTps));
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.event_query_graph_1m_tps, tags,
							queryGraphTps));

				} else if (period == fiveMinutesPeriod) {
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.event_insert_5m_tps, tags, insertTps));
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.event_query_page_5m_tps, tags,
							queryPageTps));
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.event_query_graph_5m_tps, tags,
							queryGraphTps));

				} else if (period == fifteenMinutesPeriod) {
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.event_insert_15m_tps, tags, insertTps));
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.event_query_page_15m_tps, tags,
							queryPageTps));
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.event_query_graph_15m_tps, tags,
							queryGraphTps));

				}
				if (metrics.size() > 0)
					postPerfMetrics(metrics);
			} catch (Throwable e) {
				logger.warn("monitor period: " + period + "sec ,monitor handling capacity task exception:{}", e);
			}
		}
	}

	private class FaileCountThread implements Runnable {

		@Override
		public void run() {
			try {
				double insertFailedTps = 0;
				double queryFailedTps = 0l;
				long lastInsertFailed = insertFailed;
				long lastQueryFailed = queryFailed;
				insertFailed = eventLogic.getInsertFailedAtomic();
				queryFailed = eventLogic.getQueryFailedAtomic();
				List<EventSpanTime> list = eventLogic.getEventSpanTime();
				List<PerfMetricVO> metrics = new ArrayList<PerfMetricVO>();
				for (EventSpanTime spanTime : list) {
					long startTime = spanTime.getStartTime();
					long endTime = spanTime.getEndTime();
					long lTime = spanTime.getSpanTime();
					String tenantId = spanTime.getTenantId();
					List<String> tags = getTags("event");
					tags.add("tenant:" + tenantId);
					metrics.add(PerfMetricGenerator
							.generatePerfMetric(MonitorConstants.event_start_collect_time, tags, startTime));
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.event_end_collect_time, tags, endTime));
					metrics
							.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.event_collect_duration_time, tags, lTime));
				}
				if (insertFailed > lastInsertFailed) {
					insertFailedTps = (insertFailed - lastInsertFailed)*1.0/fifteenMinutesPeriod;
				}
				if (queryFailed > lastQueryFailed) {
					queryFailedTps = (queryFailed - lastQueryFailed)*1.0/fifteenMinutesPeriod;
				}
				List<String> tags = getTags("event");
				metrics.add(PerfMetricGenerator
						.generatePerfMetric(MonitorConstants.event_insert_5m_failed_count, tags, insertFailedTps));
				metrics.add(PerfMetricGenerator
						.generatePerfMetric(MonitorConstants.event_query_5m_failed_count, tags, queryFailedTps));
				if (metrics.size() > 0)
					postPerfMetrics(metrics);
			} catch (Throwable e) {
				logger.warn("monitor period:5 mins ,monitor handling capacity task exception:{}", e);
			}
		}
	}
}
