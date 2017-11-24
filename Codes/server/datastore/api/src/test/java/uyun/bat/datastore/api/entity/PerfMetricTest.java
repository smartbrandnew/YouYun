package uyun.bat.datastore.api.entity;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class PerfMetricTest {

	private static String TENANT_ID = "1547qwd8qfr89e5d8q2de5896784oiuj";
	private static String RESOURCE_ID = "1547qwd8qfr89e5d8q2de5896784oiuj";
	@Test
	public void test() {
		PerfMetric perfMetric = new PerfMetric();
		perfMetric.toString();
		perfMetric.getName();
		perfMetric.setName("testName");
		perfMetric.getTags();
		Map<String,String>tags = new HashMap<String, String>();
		tags.put("testKey", "testValue");
		perfMetric.addTags(tags);
		perfMetric.addTag("Key", "Value");
		perfMetric.addTenantId(TENANT_ID);
		perfMetric.addResourceId(RESOURCE_ID);
		perfMetric.getResourceId();
		Object value = new Object();
		DataPoint point = new DataPoint((long)123,value);
		point.setValue(12);
		perfMetric.addDataPoint(point);
		perfMetric.checkSynstax();
		perfMetric.getTenantId();
		perfMetric.changePrecision(12);
		perfMetric.clonePerfMetric();
	}

}
