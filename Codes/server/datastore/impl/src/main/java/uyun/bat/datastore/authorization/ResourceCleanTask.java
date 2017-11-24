package uyun.bat.datastore.authorization;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.common.config.Config;
import uyun.bat.datastore.api.entity.RelativeTime;
import uyun.bat.datastore.entity.ResourceIdTransform;
import uyun.bat.datastore.logic.DistributedUtil;
import uyun.bat.datastore.logic.LogicManager;
import uyun.bat.datastore.logic.ResourceIdTransformLogic;
import uyun.bat.datastore.logic.pacific.PacificResourceLogic;
import uyun.bat.datastore.service.ServiceManager;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * 定时清理租户过期资源任务
 * 
 * @author WIN
 *
 */
public class ResourceCleanTask {
	/*
	 * event ttl:32天 metric ttl:32天 resource:500个 监测器：10个 数据暂写死，之后调租户授权数据实现
	 * 
	 */

	private static final Logger logger = LoggerFactory.getLogger(ResourceCleanTask.class);

	private static int corePoolSize = 3;
	// 设置30分钟同步一次
	private static long period = 1800;
	// ttl 暂写死32
	private static int event_ttl = Config.getInstance().get("tenant.authority.event.ttl", 32);
	private static int metric_ttl = Config.getInstance().get("tenant.authority.metric.ttl", 32);
	@Autowired
	private PacificResourceLogic pacificResourceLogic;
	@Autowired
	private ResourceIdTransformLogic resourceIdTransformLogic;
	
	@SuppressWarnings("unused")
	private void init() {

		ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize);
		logger.info("Start a thread to clean up expire resource data......");
		service.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (!DistributedUtil.isLeader())
					return;

				try {
					logger.info("The scheduled thread start to clean expire resource data......");
					List<String> tenantIds = LogicManager.getInstance().getResourceLogic().getAllTenantId();
					for (String tenantId : tenantIds) {
						long resCount = LogicManager.getInstance().getResourceRedisService()
								.getResCountByTenantId(tenantId);
						int ttl = event_ttl > metric_ttl ? event_ttl : metric_ttl;
						List<String> resIds = LogicManager.getInstance().getResourceLogic()
								.getAuthorizationResIds(tenantId, ttl);
						// TODO 目前monitor的资源表还是老的资源id
						if (resIds != null && !resIds.isEmpty()) {
							// 获取统一资源库的相关资源Id映射数据
							List<String> uIds = new ArrayList<String>();
							for (String resId : resIds) {
								ResourceIdTransform temp = resourceIdTransformLogic.getTransformIdByIds(resId, tenantId);
								if (temp != null && temp.getUnitId() != null && temp.getUnitId().length() > 0)
									uIds.add(temp.getUnitId());
							}
							pacificResourceLogic.deleteAuthorizationRes(tenantId, uIds);
						}
						long count1 = LogicManager.getInstance().getResourceRedisService()
								.deleteAuthorizationRes(resIds);
						long count2 = LogicManager.getInstance().getResourceLogic().deleteAuthorizationRes(tenantId,
								resIds);
						long count = count1 > count2 ? count1 : count2;
						long update = resCount - count;
						if (update > 0)
							LogicManager.getInstance().getResourceRedisService().insertResCount(tenantId,
									Long.toString(update));
						else {
							long t = LogicManager.getInstance().getResourceLogic().getResCountByTenantId(tenantId);
							LogicManager.getInstance().getResourceRedisService().insertResCount(tenantId,
									Long.toString(t));
						}
						// 资源删除之后，指标也应该删除
						for (String resId : resIds) {
							List<String> metricNames = ServiceManager.getInstance().getMetricService()
									.getMetricNamesByResId(resId);
							SetMultimap<String, String> tags = HashMultimap.create();
							tags.put("resourceId", resId);
							for (String metricName : metricNames) {
								// 必须要设置删除的起始时间，暂设置为5年
								LogicManager.getInstance().getMetricClean().deleteMetricData(metricName, tags,
										new RelativeTime(5, uyun.bat.datastore.api.entity.TimeUnit.YEARS));
							}
						}
					}
					logger.info("Expire resource data clean up task finish......");
				} catch (Throwable e) {
					logger.warn("Expire resource data clean up task error:{}", e);
				}
			}
		}, 120, period, TimeUnit.SECONDS);
	}

}
