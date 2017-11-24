package uyun.bat.datastore.authorization;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.common.config.Config;
import uyun.bat.datastore.api.entity.RelativeTime;
import uyun.bat.datastore.logic.DistributedUtil;
import uyun.bat.datastore.logic.LogicManager;
import uyun.bat.datastore.service.ServiceManager;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class MetricCleanTask {
	/*
	 * event ttl:32天 metric ttl:32天 resource:500个 监测器：10个 数据暂写死，之后调租户授权数据实现
	 */
	private static final Logger logger = LoggerFactory.getLogger(MetricCleanTask.class);
	private static int corePoolSize = 3;
	// 设置30分钟同步一次
	private static long period = 1800;
	// ttl 暂写死32
	private static int ttl = Config.getInstance().get("tenant.authority.metric.ttl", 32);

	@SuppressWarnings("unused")
	private void init() {
		logger.info("Starting a scheduled thread to clean expire metric data.......");
		ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize);
		service.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (!DistributedUtil.isLeader())
					return;
				
				try {
					logger.info("The scheduled thread start to clean expire metric data ......");
					List<String> tenantIds = LogicManager.getInstance().getResourceLogic().getAllTenantId();
					for (String tenantId : tenantIds) {
						List<String> metricNames = ServiceManager.getInstance().getMetricService()
								.getMetricNamesByTenantId(tenantId);
						SetMultimap<String, String> tags = HashMultimap.create();
						tags.put("tenantId", tenantId);
						for (String metricName : metricNames) {
							// 必须要设置删除的起始时间，暂设置为5年
							if (metricName != null && metricName.trim().length() > 0) {
								LogicManager
										.getInstance()
										.getMetricClean()
										.deleteMetricData(metricName, tags,
												new RelativeTime(5, uyun.bat.datastore.api.entity.TimeUnit.YEARS),
												new RelativeTime(ttl, uyun.bat.datastore.api.entity.TimeUnit.DAYS));
							}
						}
					}
					logger.info("Expire metric data clean up task finish......");
				} catch (Throwable e) {
					logger.warn("Expire metric data clean up task error:{}", e);
				}
			}
		}, 120, period, TimeUnit.SECONDS);
	}

}
