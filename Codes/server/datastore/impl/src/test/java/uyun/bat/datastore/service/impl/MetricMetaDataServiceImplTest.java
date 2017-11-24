package uyun.bat.datastore.service.impl;

import org.junit.Test;

import uyun.bat.datastore.Startup;
import uyun.bat.datastore.api.entity.MetricMetaData;
import uyun.bat.datastore.api.entity.MetricType;
import uyun.bat.datastore.api.service.MetricMetaDataService;

public class MetricMetaDataServiceImplTest {
	private static MetricMetaDataService service = (MetricMetaDataService) Startup.getInstance().getBean(
			"metricMetaDataService");

	@Test
	public void testInsert() {
		service.insert(new MetricMetaData("test.cpu.usage", "mbps", 1d, 100d, 2, MetricType.gauge,"","","test","e0a67e986a594a61b3d1e523a0a39c77"));
	}

	@Test
	public void testUpdate() {
		service.update(new MetricMetaData("test.cpu.usage", "mbps", 1d, 100d, 4, MetricType.gauge,"","","test","e0a67e986a594a61b3d1e523a0a39c77"));
	}

	@Test
	public void testDelete() {
		service.delete("test.cpu.usage");
	}

	@Test
	public void testQueryByName() {
		service.queryByName("system.load.5");
	}

	@Test
	public void testQueryAll() {
		service.queryAll("e0a67e986a594a61b3d1e523a0a39c77");
	}
	
	@Test
	public void testGetMetricMetaDataByKey() {
		service.getMetricMetaDataByKey("mysql");
	}
}
