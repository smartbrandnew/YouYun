package uyun.bat.monitor.impl.logic;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import uyun.bat.common.config.Config;
import uyun.bat.common.selfmonitor.HTTPClientUtils;
import uyun.bat.monitor.api.entity.AutoRecoverRecord;
import uyun.bat.monitor.impl.common.DistributedUtil;
import uyun.bat.monitor.impl.common.ServiceManager;
import uyun.bird.tenant.api.entity.ApiKey;
import uyun.bird.tenant.api.entity.Tenant;

public class AutoRecoverTask implements ApplicationListener<ContextRefreshedEvent> {
	private static Logger logger = LoggerFactory.getLogger(AutoRecoverRecord.class);

	public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
			//初始化分布式工具类
			Class.forName(DistributedUtil.class.getName());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		// 是否向automation推送数据
		if (Config.getInstance().get("auto.push.mode", false)) {
			logger.info("Push data to Automation task thread start......");
			timerPushData();
		}
	}

	private void timerPushData() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if (DistributedUtil.isLeader()) {
					List<AutoRecoverRecord> autoRecoverRecords = LogicManager.getInstance().getAutoRecoverRecordLogic()
							.getAutoRecoverRecordList();
					if (autoRecoverRecords != null && autoRecoverRecords.size() > 0) {
						for (AutoRecoverRecord arr : autoRecoverRecords) {
							if (arr.getParams().length() == 0)
								return;
							generateData(arr.getTenantId(), arr.getMonitorName(), arr.getHostName(), arr.getParams(),
									arr.getExecuteId(), arr.getInterval());
						}
					}
				}
			}
		};
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(task, 0, 60 * 1000);
	}

	public void generateData(String tenantId, String monitorName, String hostName, String params, String executeId,
			long interval) {
		Tenant t = new Tenant();
		try {
			t = ServiceManager.getInstance().getTenantService().view(tenantId);
		} catch (Exception e) {
			logger.error("Fail to query tenant" + e);
			return;
		}
		List<ApiKey> apiKeys = t.getApiKeys();
		// 161014目前一个租户只对应一个apikey
		if (null == apiKeys || apiKeys.isEmpty())
			logger.error("apiKey is null");
		String apikey = apiKeys.get(0).getKey();
		String url = Config.getInstance().get("auto.openapi").toString();
		try {
			if (interval != 0) {
				logger.info("Tenant:" + tenantId + " Monitor：" + monitorName + " Host：" + hostName + " exception，Execute the specified choreography task......");
				logger.info("Task ID:" + executeId + " Data：" + params + " ,url:" + url);
			} else if (logger.isDebugEnabled()) {
				logger.debug("Tenant:" + tenantId + " Monitor：" + monitorName + " exception，Execute the specified choreography task......");
				logger.debug("Task ID:" + executeId + " Data：" + params + " ,url:" + url);
			}
			HTTPClientUtils.post(url + "?apikey=" + apikey + "&id=" + executeId, params);
		} catch (Exception e) {
			logger.warn("Push data to Automation failed....");
		}
	}
}
