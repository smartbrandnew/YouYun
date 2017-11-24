package com.broada.carrier.monitor.probe.impl.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.impl.config.Config;

public class ResourceDetailUploadTask {
	
	private static Logger logger = LoggerFactory.getLogger(ResourceDetailUploadTask.class);
	private static int corePoolSize = 2;
	// 同步周期为5分钟
	private static long period = Config.getDefault().getProperty("resource.detail.intake.period", 1 * 60);

	private static long delay = 1;
	@Autowired
	private ResourceDetailUploadService service;

	protected void init() {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize);
		service.scheduleAtFixedRate(new WorkThread(), delay, period, TimeUnit.SECONDS);
	}

	class WorkThread implements Runnable {
		@Override
		public void run() {
			try {
				service.uploadResourceDetail();
			} catch (Throwable e) {
				logger.error("执行定时任务上报资源详情失败:", e);
			}
		}
	}
}
