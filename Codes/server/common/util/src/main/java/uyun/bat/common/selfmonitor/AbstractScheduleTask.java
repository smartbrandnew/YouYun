package uyun.bat.common.selfmonitor;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.common.config.Config;

public abstract class AbstractScheduleTask {
	private static final Logger logger = LoggerFactory.getLogger(AbstractScheduleTask.class);
	public static int oneMinutesPeriod = 60;//60
	public static int fiveMinutesPeriod = 300;
	public static int fifteenMinutesPeriod = 900;
	public static int corePoolSize = 2;
	public int initDelay = 120;
	public static String default_charset = "UTF-8";
	private static final String perf_url = Config.getInstance().get("selfmonitor.openapi.perfmetrics", "");
	private static ObjectMapper mapper = new ObjectMapper();

	public abstract void init();

	public List<String> getTags(String moduleName) {
		List<String> list = new ArrayList<String>();
		list.add("selfmoitor:" + moduleName);
		try {
			String ip = InetAddress.getLocalHost().toString();
			if (ip != null)
				list.add("ip:" + ip);
		} catch (Exception e) {
			logger.warn("gain host message error: " + e);
		}
		return list;
	}

	public boolean postPerfMetrics(List<PerfMetricVO> metrics) {
		try {

			String json = mapper.writeValueAsString(metrics);
			String sign = HTTPClientUtils.post(perf_url, json);
			if (sign == null) {
				logger.warn("fail to report selfmonitor metrics,count:{}", metrics.size());
				return false;
			}
			return true;
		} catch (JsonProcessingException e1) {
			logger.warn("json convert exception: ", e1);
		} catch (Exception e) {
			logger.warn("exception: ", e);
		}
		return false;
	}

}
