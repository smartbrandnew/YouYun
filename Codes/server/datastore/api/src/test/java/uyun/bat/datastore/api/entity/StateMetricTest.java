package uyun.bat.datastore.api.entity;

import static org.junit.Assert.*;

import org.junit.Test;

public class StateMetricTest {

	@Test
	public void test() {
		StateMetric stateMetric = new StateMetric("name", "value",(long)12);
		stateMetric.setName("testname");
		stateMetric.setTimestamp((long)123);
		stateMetric.setValue("testvalue");
		stateMetric.addTenantId("123");
		stateMetric.addResourceId("12");
		stateMetric.addTag("key","value");
		stateMetric.getName();
		stateMetric.getResourceId();
		stateMetric.getTags();
		stateMetric.getTenantId();
		stateMetric.getTimestamp();
		stateMetric.getValue();
	}

}
