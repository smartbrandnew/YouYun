package uyun.bat.datastore.api.entity;

import org.junit.Assert;
import org.junit.Test;

public class MetricBuilderTest {

	@Test
	public void testGetInstance() {
		PerfMetricBuilder builder = PerfMetricBuilder.getInstance();
		Assert.assertNotNull(builder);
	}

	@Test
	public void testAddMetric() {
		PerfMetricBuilder builder = PerfMetricBuilder.getInstance();
		builder.addMetric("aa");
		Assert.assertEquals(1, builder.getMetrics().size());
	}

	@Test
	public void testGetMetrics() {
		PerfMetricBuilder builder = PerfMetricBuilder.getInstance();
		builder.addMetric("aa");
		Assert.assertNotNull(builder.getMetrics());
	}

}
