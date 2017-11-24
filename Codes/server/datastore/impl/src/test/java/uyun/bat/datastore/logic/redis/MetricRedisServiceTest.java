package uyun.bat.datastore.logic.redis;

import java.util.ArrayList;

import org.junit.Test;

import uyun.bat.datastore.Startup;

;

public class MetricRedisServiceTest {
	private static MetricRedisService metricRedisService = (MetricRedisService) Startup.getInstance().getBean(
			"metricRedisService");
	/**
	 * id必须32位
	 */
	private static final String TENANT_ID = "12345678910111213141516171819202";
	private static final String RESOURCE_ID = "12345678910111213141516171819203";

	@Test
	public void testGetMetricNamesByResId() {
		metricRedisService.getMetricNamesByResId(RESOURCE_ID);
	}

	@Test
	public void testGetMetricNamesByTenantIdString() {
		metricRedisService.getMetricNamesByTenantId(TENANT_ID);
	}

	@Test
	public void testAddMetricNames() {
		metricRedisService.addMetricNames(new String[] { "sys.cpu.usage" }, RESOURCE_ID, TENANT_ID);
	}

	@Test
	public void testGetResourceMetric() {
		metricRedisService.getResourceMetric(RESOURCE_ID);
	}

	@Test
	public void testDeleteMetricNamesByResId() {
		metricRedisService.deleteMetricNamesByResId(RESOURCE_ID);
	}

	@Test
	public void testDeleteMetricNamesBatch() {
		metricRedisService.deleteMetricNamesBatch(new ArrayList<String>());
	}

	@Test
	public void testDeleteMetricResIds() {
		metricRedisService.deleteMetricResIds("aaa");
	}

	@Test
	public void testGetAsyncMetricNames() {
		metricRedisService.getAsyncMetricNames();
	}

}
