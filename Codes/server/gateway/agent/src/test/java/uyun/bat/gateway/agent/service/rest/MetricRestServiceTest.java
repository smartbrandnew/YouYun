package uyun.bat.gateway.agent.service.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import uyun.bat.gateway.agent.entity.PerfMetricVO;
import uyun.bat.gateway.agent.httpservletutil.HttpServletTest;
import uyun.bat.gateway.agent.service.openapi.MetricRestService;
import uyun.bat.gateway.agent.servicetest.CustomMetricServiceTest;
import uyun.bat.gateway.api.service.ServiceManager;

public class MetricRestServiceTest {
	MetricRestService service = null;

	@Before
	public void setUp() throws Exception {
		service = new MetricRestService();
		ServiceManager.getInstance().setCustomMetricService(new CustomMetricServiceTest());
	}

	@Test
	public void testIntakePerfMetric() {
		List<PerfMetricVO> metrics = new ArrayList<PerfMetricVO>();
		PerfMetricVO pvo = new PerfMetricVO();
		metrics.add(pvo);
		pvo.setMetric("testIntakePerfMetric");
		List<String> tags = new ArrayList<String>();
		tags.add("location:a");
		tags.add("level:high");
		pvo.setTags(tags);
		HttpServletRequest request = new HttpServletTest();
		service.intakePerfMetric(metrics, request);
	}
}
