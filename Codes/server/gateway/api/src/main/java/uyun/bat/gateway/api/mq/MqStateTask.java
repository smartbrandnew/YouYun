package uyun.bat.gateway.api.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MqStateTask {
	private static final Logger logger = LoggerFactory.getLogger(MqStateTask.class);

	private static volatile boolean isHealthy = true;

	public void init() {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(new Runnable() {

			public void run() {
				MqCrawler mqMonitor = null;
				try {
					mqMonitor = new MqCrawler();
					isHealthy = mqMonitor.isHealthy();
					if (!isHealthy) {
						logger.error(mqMonitor.getInfo());
					}
				} catch (Exception e) {
					logger.error("gain MQ state exception:{}", e.getMessage());
					if (logger.isDebugEnabled())
						logger.debug("Stack: ", e);
				}
			}
		}, 0, 60, TimeUnit.SECONDS);

	}

	public static boolean isHealthy() {
		return isHealthy;
	}

	public static void main(String[] args) {
		new MqStateTask().init();
	}

}
