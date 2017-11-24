package uyun.bat.report.impl.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.bat.report.api.entity.Report;
import uyun.bat.report.api.entity.ReportConstant;
import uyun.bat.report.api.service.ReportService;
import uyun.bat.report.impl.dao.redis.RedisDao;
import uyun.bat.report.impl.facade.FacadeManager;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduleTask {

	Logger log = LoggerFactory.getLogger(ScheduleTask.class);

	static final Integer delay = 20;
	static final Integer period = 3;
	static final Integer failedRetry = 3;

	@Resource
	ReportService reportService;
	@Resource
	RedisDao redisDao;

	public void init() {
		generateReportDailyCheck();
	}

	public void generateReportDailyCheck() {
		log.info("<==report daily checking begin==>");
		List<Report> reports = FacadeManager.getInstance().getReportFacade().getAllValidReport();
		if (reports != null && reports.size() > 0) {
			// 获取redis分布锁保持360秒
			boolean getLock = redisDao.getDistributeLock(ReportConstant.REDIS_REPORT_LOCK_KEY,
					ReportConstant.REDIS_REPORT_LOCKING, ReportConstant.REDIS_REPORT_LOCK_EXPIRE);
			if (getLock) {
				for (Report report : reports) {
					String reportId = report.getReportId();
					log.info("execute reportId:" + reportId + "|" + report.getReportName());
					try {
						boolean flag = reportService.createReportDataAllSync(report, true);
						if (!flag) {
                            log.info("report store failed waiting for retry ....");
                            redisDao.sadd(ReportConstant.REDIS_REPORT_FAILED_SET, reportId);
                        }
					} catch (Exception e) {
						log.warn("report generate failed", e);
						redisDao.sadd(ReportConstant.REDIS_REPORT_FAILED_SET, reportId);
					}
				}
				// Mission Complete释放完成信号
				redisDao.setex(ReportConstant.REDIS_REPORT_LOCK_KEY,
						ReportConstant.REDIS_REPORT_LOCK_EXPIRE, ReportConstant.REDIS_REPORT_COMPLETE);
			}
			retryGenerateReport();
		}
		log.info("<==report daily checking end==>");
	}

	/**
	 * 检查失败 redis set 重试进程
	 */
	private void retryGenerateReport() {
		log.info("<==report daily retry begin==>");
		// 若获取锁失败则开始捡漏模式
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				int retry = 1;
				Long count = redisDao.scard(ReportConstant.REDIS_REPORT_FAILED_SET);
				String signal = redisDao.get(ReportConstant.REDIS_REPORT_LOCK_KEY);
				if (count == 0 && ReportConstant.REDIS_REPORT_COMPLETE.equals(signal)) {
					service.shutdown();
				} else if (count > 0) {
					String reportId = redisDao.spop(ReportConstant.REDIS_REPORT_FAILED_SET);
					if (reportId != null && reportId.length() == 32) {
						Report r = reportService.queryReportById(null, reportId);
						log.info("re-execute reportId:" + reportId + "|" + r.getReportName());
						// 如果仍失败重试三次
						while (retry <= failedRetry) {
							if (retry == failedRetry) {
								// 最后一次重试不再检查
								reportService.createReportDataAllSync(r, false);
								break;
							}
							boolean flag = reportService.createReportDataAllSync(r, true);
							if (flag) {
								break;
							}
							retry++;
						}
					}
				}
			}
		}, delay, period, TimeUnit.SECONDS);
		try {
			// 检查360秒后shutdown
			Thread.sleep(ReportConstant.REDIS_REPORT_LOCK_EXPIRE * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (!service.isShutdown()) {
				service.shutdown();
			}
		}
	}

	public void removeAllInvalidFiles() {
		reportService.removeAllInvalidFiles();
	}

	// 测试服务器集群竞争锁
	public void test() {
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				generateReportDailyCheck();
			}
		});
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				generateReportDailyCheck();
			}
		});
		t1.start();
		t2.start();
	}

}
