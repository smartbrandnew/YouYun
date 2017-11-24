package com.broada.carrier.monitor.probe.impl.sync;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.sync.service.ProbeInfoSyncService;
/**
 * 周期向monitor server推送probe信息数据
 * @author WIN
 *
 */
public class ProbeInfoTransTask {
	private static Logger logger = LoggerFactory.getLogger(ProbeInfoTransTask.class);
	private static int corePoolSize = 2;
	// 同步周期
	private static long period = Config.getDefault().getProperty("agent.info.intake.period", 60);

	private static long delay = 60;

	protected void init() {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize);
		service.scheduleAtFixedRate(new WorkThread(), delay, period, TimeUnit.SECONDS);
	}

	class WorkThread implements Runnable {

		@Override
		public void run() {
			try {
				ProbeInfoSyncService.getInstance().postProbeInfo();
				logger.info("probe周期发送信息到server端成功");
			} catch (Throwable e) {
				logger.warn("probe周期发送信息到server端异常: ", e);
			}
		}
	}
}
