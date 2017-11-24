package uyun.bat.monitor.selfmonitor;

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
import uyun.bat.monitor.core.logic.CheckController;

public class ScheduleTask extends AbstractScheduleTask {
	private static final Logger logger = LoggerFactory.getLogger(ScheduleTask.class);
	private long length;

	public void init() {
		if (Config.getInstance().get("selfmonitor.start", false)) {
			logger.info("Selfmonitor process start......");
			length = CheckController.getInstance().getMonitorTps();
			ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize);
			service
					.scheduleAtFixedRate(new WorkThread(oneMinutesPeriod, length), initDelay, oneMinutesPeriod, TimeUnit.SECONDS);
			service.scheduleAtFixedRate(new WorkThread(fiveMinutesPeriod, length), initDelay, fiveMinutesPeriod,
					TimeUnit.SECONDS);
			service
					.scheduleAtFixedRate(new WorkThread(fifteenMinutesPeriod, length), initDelay, fifteenMinutesPeriod,
							TimeUnit.SECONDS);
		}
	}

	private class WorkThread implements Runnable {
		private int period;
		private long len;

		public WorkThread(int period, long len) {
			this.period = period;
			this.len = len;
		}

		@Override
		public void run() {
			try {
				double tps = 0;
				long lastLength = len;
				len = CheckController.getInstance().getMonitorTps();
				if (len > lastLength) {
					tps = (len - lastLength) * 1.0 / period;
				}
				List<String> tags = getTags("monitor");
				List<PerfMetricVO> metrics = new ArrayList<PerfMetricVO>();
				if (period == oneMinutesPeriod) {
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.monitor_deal_1m_tps, tags, tps));
				} else if (period == fiveMinutesPeriod) {
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.monitor_deal_5m_tps, tags, tps));
				} else if (period == fifteenMinutesPeriod) {
					metrics.add(PerfMetricGenerator.generatePerfMetric(MonitorConstants.monitor_deal_15m_tps, tags, tps));
				}
				if (metrics.size() > 0)
					postPerfMetrics(metrics);
			} catch (Throwable e) {
				logger.warn("Monitor task period: " + period + "sec ,monitor handling capacity task exception:{}", e);
			}
		}
	}

}
