package uyun.bat.datastore.api.entity;



import org.junit.Assert;
import org.junit.Test;

public class MetricTest {

	@Test
	public void testAddTag() {
		PerfMetric perfMetric=new PerfMetric("cpu");
		perfMetric.addDataPoint(new DataPoint(System.currentTimeMillis(), 23.5)).addTag("host", "myPC");
		Assert.assertEquals("myPC", perfMetric.getTags().get("host").get(0));;
	}

	@Test
	public void testCheckSynstax() {
		PerfMetric perfMetric=new PerfMetric("cpu");
		perfMetric.addDataPoint(new DataPoint(System.currentTimeMillis(), 23.5)).addTag("host", "myPC");
		Assert.assertNotNull(perfMetric);
	}

}
