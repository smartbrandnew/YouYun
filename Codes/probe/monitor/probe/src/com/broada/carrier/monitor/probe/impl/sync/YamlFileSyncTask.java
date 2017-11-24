package com.broada.carrier.monitor.probe.impl.sync;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.sync.service.YamlFileSyncService;


/**
 * 周期检测yaml文件是否需要更新
 * @author WIN
 *
 */
public class YamlFileSyncTask {
	private static Logger logger = LoggerFactory.getLogger(YamlFileSyncTask.class);
	private static int corePoolSize = 2;
	// 同步周期
	private static long period = Config.getDefault().getProperty("agent.yaml.update.period", 120);

	private static long delay = 60;

	protected void init() {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize);
		service.scheduleAtFixedRate(new WorkThread(), delay, period, TimeUnit.SECONDS);
	}

	class WorkThread implements Runnable {

		@Override
		public void run() {
			try {
				logger.info("检测服务端yaml文件更新任务开始执行......");
				YamlFileSyncService.getInstance().sync();
				logger.info("检测服务端yaml文件更新任务执行完成......");
			} catch (Throwable e) {
				logger.warn("周期检测更新Yaml文件任务异常:", e);
			}
		}
	}
}
