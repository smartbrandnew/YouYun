package com.broada.carrier.monitor.probe.impl.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.sync.YamlFileSyncTask;

/**
 * 定时上报主机资源
 * @author admin
 */
public class ResourceUploadTask {
	private static Logger logger = LoggerFactory.getLogger(YamlFileSyncTask.class);
	private static int corePoolSize = 2;
	// 同步周期为5分钟
	private static long period = Config.getDefault().getProperty("host.intake.period", 5 * 60);

	private static long delay = 1;
	@Autowired
	private ResourceUploadService service;

	protected void init() {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize);
		service.scheduleAtFixedRate(new WorkThread(), delay, period, TimeUnit.SECONDS);
	}

	class WorkThread implements Runnable {

		@Override
		public void run() {
			try {
				service.uploadResource();
			} catch (Throwable e) {
				logger.warn("执行定时任务上报资源失败:", e);
			}
		}
	}
	
}
