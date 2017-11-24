package uyun.bat.datastore.selfmonitor;


import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uyun.bat.common.selfmonitor.PerfMetricVO;
import uyun.bat.datastore.Startup;

public class ScheduleTaskTest {
	private static ScheduleTask task=Startup.getInstance().getBean(ScheduleTask.class);
	
	@Test
	public void testInit() {
		task.init();
	}

	@Test
	public void testGetTags() {
		task.getTags("datastore");
	}

	@Test
	public void testPostPerfMetrics() {
		List<PerfMetricVO> metrics=new ArrayList<PerfMetricVO>();
		task.postPerfMetrics(metrics);
	}

}
