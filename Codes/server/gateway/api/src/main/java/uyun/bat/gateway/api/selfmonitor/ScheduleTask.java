package uyun.bat.gateway.api.selfmonitor;

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
import uyun.bat.gateway.api.service.ServiceManager;

public class ScheduleTask extends AbstractScheduleTask {
	private static final Logger logger = LoggerFactory.getLogger(ScheduleTask.class);
	private long metricLength;
	private long eventLength;

	public void init() {
		if (Config.getInstance().get("selfmonitor.start", false)) {
			logger.info("SlefMonitor process start......");
			metricLength = ServiceManager.getInstance().getMetricSize();
			eventLength = ServiceManager.getInstance().getEventSize();
			ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize);
			service.scheduleAtFixedRate(new WorkThread(oneMinutesPeriod, metricLength, eventLength), initDelay,
					oneMinutesPeriod, TimeUnit.SECONDS);
			service.scheduleAtFixedRate(new WorkThread(fiveMinutesPeriod, metricLength, eventLength), initDelay,
					fiveMinutesPeriod, TimeUnit.SECONDS);
			service
					.scheduleAtFixedRate(new WorkThread(fifteenMinutesPeriod, metricLength, eventLength), initDelay,
							fifteenMinutesPeriod, TimeUnit.SECONDS);
		}
	}

	private class WorkThread implements Runnable {
		private int period;
		private long metricLen;
		private long eventLen;

		public WorkThread(int period, long metricLen, long eventLen) {
			this.period = period;
			this.metricLen = metricLen;
			this.eventLen = eventLen;
		}

		@Override
		public void run() {
			try {
				double metricTps = 0;
				double eventTps = 0;
				long lastMetricLength = metricLen;
				long lastEventLength = eventLen;
				metricLen = ServiceManager.getInstance().getMetricSize();
				eventLen = ServiceManager.getInstance().getEventSize();
				if (metricLen > lastMetricLength) {
					metricTps = (metricLen - lastMetricLength) * 1.0 / period;
				}
				if (eventLen > lastEventLength) {
					eventTps = (eventLen - lastEventLength) * 1.0 / period;
				}
				List<String> tags = getTags("gateway");
				List<PerfMetricVO> metrics = new ArrayList<PerfMetricVO>();
				if (period == oneMinutesPeriod) {
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.gateway_metric_insert_1m_tps, tags,
							metricTps));
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.gateway_event_insert_1m_tps, tags,
							eventTps));

				} else if (period == fiveMinutesPeriod) {
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.gateway_metric_insert_5m_tps, tags,
							metricTps));
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.gateway_event_insert_5m_tps, tags,
							eventTps));

				} else if (period == fifteenMinutesPeriod) {

					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.gateway_metric_insert_15m_tps, tags,
							metricTps));
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.gateway_event_insert_15m_tps, tags,
							eventTps));
				}
				if (metrics.size() > 0)
					postPerfMetrics(metrics);
			} catch (Throwable e) {
				logger.warn("monitor period: " + period + "sec ,monitor handling capacity task exception:{}", e);
			}
		}
	}
}
