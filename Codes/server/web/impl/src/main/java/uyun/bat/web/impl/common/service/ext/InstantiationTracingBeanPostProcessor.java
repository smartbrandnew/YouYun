package uyun.bat.web.impl.common.service.ext;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import uyun.bat.common.config.Config;
import uyun.bat.common.leaderselector.BatLeaderSelector;
import uyun.bat.dashboard.api.entity.DashboardCount;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.ResourceCount;
import uyun.bat.monitor.api.entity.MonitorCountVO;
import uyun.bat.web.impl.common.service.ServiceManager;
import uyun.bird.tenant.api.entity.Product;

public class InstantiationTracingBeanPostProcessor implements ApplicationListener<ContextRefreshedEvent> {
	private static Logger logger = LoggerFactory.getLogger(InstantiationTracingBeanPostProcessor.class);

	@SuppressWarnings("static-access")
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Product producy_bat = context.getBean(Product.class);
		if (producy_bat == null) {
			logger.error("Product does not exist! process aborted");
			System.exit(1);
		}
		logger.info("Pushdata to Protal task thread Start......");
		timerPushTask();
		
		int retry = 6;
		do {
			if (register(producy_bat))
				break;
			try {
				// 等10秒准备重连租户
				Thread.currentThread().sleep(10 * 1000);
			} catch (Throwable e) {
				// 吃掉
			}
			retry = retry - 1;
		} while (retry > 0);
	}

	private boolean register(Product producy_bat) {
		try {
			ServiceManager.getInstance().getProductService().save(producy_bat);
			return true;
		} catch (Throwable e) {
			logger.error("Product fail to register！" + e.getMessage());
			if (logger.isDebugEnabled())
				logger.debug("Stack：", e);
			return false;
		}
	}

	private void timerPushTask() {
		BatLeaderSelector selector = new BatLeaderSelector("/uyun.bat.web.pushDataToPortal");
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if (selector.isLeader())
					pushDataToProtal();
			}
		};
		Timer timer = new Timer();
		// 每十分钟执行
		long intevalPeriod = 10 * 60 * 1000;
		long delay = intevalPeriod;
		timer.scheduleAtFixedRate(task, delay, intevalPeriod);
	}

	private void pushDataToProtal() {
		Map<String, Map<String, String>> map = new HashMap<>();

		Date endTime = new Date();
		// 当天
		Date startDayTime = compareDate(endTime, 0);
		// 当月
		Date startMonTime = compareDate(endTime, 1);
		logger.info("Day：" + startDayTime + ",Month：" + startMonTime + ",End：" + endTime);
		// 分别获取仪表盘当天、当月以及累计总数
		List<DashboardCount> curDayDashCounts = ServiceManager.getInstance().getDashboardService()
				.getDashboardCountByDate(startDayTime, endTime);
		List<DashboardCount> curMonDashCounts = ServiceManager.getInstance().getDashboardService()
				.getDashboardCountByDate(startMonTime, endTime);
		List<DashboardCount> dashCounts = ServiceManager.getInstance().getDashboardService().getDashboardCount();
		// 分别获取资源当天、当月、在线状态以及累计总数
		List<ResourceCount> curDayResCounts = ServiceManager.getInstance().getResourceService()
				.getResCountByDate(startDayTime, endTime);
		List<ResourceCount> curMonResCounts = ServiceManager.getInstance().getResourceService()
				.getResCountByDate(startMonTime, endTime);
		List<ResourceCount> onlineCounts = ServiceManager.getInstance().getResourceService()
				.getResCountByOnlineStatus(OnlineStatus.ONLINE);
		List<ResourceCount> resCounts = ServiceManager.getInstance().getResourceService().getResCount();
		// 分别获取监测器当天、当月以及累计总数
		List<MonitorCountVO> curDayMonitorCounts = ServiceManager.getInstance().getMonitorService()
				.getMonitorCountByDate(startDayTime, endTime);
		List<MonitorCountVO> curMonMonitorCounts = ServiceManager.getInstance().getMonitorService()
				.getMonitorCountByDate(startMonTime, endTime);
		List<MonitorCountVO> monitorCounts = ServiceManager.getInstance().getMonitorService().getMonitorCount();

		getDashValue(curDayDashCounts, "curDayDashCounts", map);
		getDashValue(curMonDashCounts, "curMonDashCounts", map);
		getDashValue(dashCounts, "dashCounts", map);

		getResValue(curDayResCounts, "curDayResCounts", map);
		getResValue(curMonResCounts, "curMonResCounts", map);
		getResValue(onlineCounts, "onlineCounts", map);
		getResValue(resCounts, "resCounts", map);

		getMonitorValue(curDayMonitorCounts, "curDayMonitorCounts", map);
		getMonitorValue(curMonMonitorCounts, "curMonMonitorCounts", map);
		getMonitorValue(monitorCounts, "monitorCounts", map);

		List<Map<String, String>> statistics = new ArrayList<>();
		for (String t : map.keySet()) {
			Map<String, String> map1 = new HashMap<>();
			map1.put("productNum", Config.getInstance().get("product.bat.productNum").toString());
			map1.put("tenantId", t);
			map1.put("creUserId", null);

			map1.put("curDayDashCounts", map.get(t).get("curDayDashCounts") == null ? "0" : map.get(t)
					.get("curDayDashCounts"));
			map1.put("curMonDashCounts", map.get(t).get("curMonDashCounts") == null ? "0" : map.get(t)
					.get("curMonDashCounts"));
			map1.put("dashCounts", map.get(t).get("dashCounts") == null ? "0" : map.get(t).get("dashCounts"));

			map1.put("curDayResCounts", map.get(t).get("curDayResCounts") == null ? "0" : map.get(t).get("curDayResCounts"));
			map1.put("curMonResCounts", map.get(t).get("curMonResCounts") == null ? "0" : map.get(t).get("curMonResCounts"));
			map1.put("onlineCounts", map.get(t).get("onlineCounts") == null ? "0" : map.get(t).get("onlineCounts"));
			map1.put("resCounts", map.get(t).get("resCounts") == null ? "0" : map.get(t).get("resCounts"));

			map1.put("curDayMonitorCounts",
					map.get(t).get("curDayMonitorCounts") == null ? "0" : map.get(t).get("curDayMonitorCounts"));
			map1.put("curMonMonitorCounts",
					map.get(t).get("curMonMonitorCounts") == null ? "0" : map.get(t).get("curMonMonitorCounts"));
			map1.put("monitorCounts", map.get(t).get("monitorCounts") == null ? "0" : map.get(t).get("monitorCounts"));
			statistics.add(map1);
		}
		try {
			logger.info("data trim finish,push to Protal......");
			ServiceManager.getInstance().getProductService().updateStatistics(statistics);
			for (int i = 0; i < statistics.size(); i++) {
				logger.info("Push data：" + statistics.get(i));
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to push data to Protal");
		}

	}

	private void getDashValue(List<DashboardCount> counts, String name, Map<String, Map<String, String>> map) {
		for (DashboardCount c : counts) {
			Map<String, String> temp = new HashMap<>();
			if (map.get(c.getTenantId()) == null) {
				temp.put(name, c.getCount());
			} else {
				temp.putAll(map.get(c.getTenantId()));
				temp.put(name, c.getCount());
			}
			map.put(c.getTenantId(), temp);
		}
	}

	private void getMonitorValue(List<MonitorCountVO> counts, String name, Map<String, Map<String, String>> map) {
		for (MonitorCountVO m : counts) {
			Map<String, String> temp = new HashMap<>();
			if (map.get(m.getTenantId()) == null) {
				temp.put(name, m.getCount());
			} else {
				temp.putAll(map.get(m.getTenantId()));
				temp.put(name, m.getCount());
			}
			map.put(m.getTenantId(), temp);
		}
	}

	private void getResValue(List<ResourceCount> counts, String name, Map<String, Map<String, String>> map) {
		for (ResourceCount r : counts) {
			Map<String, String> temp = new HashMap<>();
			if (map.get(r.getTenantId()) == null) {
				temp.put(name, r.getCount());
			} else {
				temp.putAll(map.get(r.getTenantId()));
				temp.put(name, r.getCount());
			}
			map.put(r.getTenantId(), temp);
		}
	}

	/**
	 * 校验时间
	 */
	private static Date compareDate(Date from, int i) {
		Calendar c1 = Calendar.getInstance();
		try {
			c1.setTime(from);
		} catch (Exception e3) {
			throw new IllegalArgumentException("Time conversion error!");
		}
		// 0获取前一个月 1获取前一天
		if (i == 1)
			c1.add(Calendar.MONTH, -1);
		if (i == 0)
			c1.add(Calendar.DAY_OF_MONTH, -1);
		return c1.getTime();
	}

	public static void main(String[] args) {
		// 测试用
		// Startup.getInstance().startup();
		// pushDataToProtal();
	}
}
