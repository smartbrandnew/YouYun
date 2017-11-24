package uyun.bat.gateway.agent.service.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import uyun.bat.datastore.api.mq.ResourceInfo;
import uyun.bat.gateway.agent.entity.*;
import uyun.bat.gateway.agent.entity.newentity.*;
import uyun.bat.gateway.agent.httpservletutil.HttpServletTest;
import uyun.bat.gateway.agent.service.api.AgentService;
import uyun.bat.gateway.agent.service.openapi.AgentRESTService;
import uyun.bat.gateway.agent.servicetest.*;
import uyun.bat.gateway.api.common.GatewayConstants;
import uyun.bat.gateway.api.service.CustomResourceService;
import uyun.bat.gateway.api.service.ServiceManager;

public class AgentRESTServiceTest {
	AgentService as = new AgentRESTService();
	private static String ID = UUID.randomUUID().toString();
	@Before
	public void setUp() throws Exception {
		ServiceManager.getInstance().setCustomMetricService(new CustomMetricServiceTest());
		ServiceManager.getInstance().setMetricService(MetricServiceTest.create());
		ServiceManager.getInstance().setResourceService(ResourceServiceTest.create());
		ServiceManager.getInstance().setEventService(EventServiceTest.create());
		ServiceManager.getInstance().setStateService(StateServiceTest.create());
		ServiceManager.getInstance().setMetricMetaDataService(MetricMetaDataServiceTest.create());
		ServiceManager.getInstance().setCustomResourceService(new CustomResourceService() {
			public void readOffline(ResourceInfo resourceInfo) {
				return;
			}
		});
	}

	@Test
	public void testOldIntakePerfMetric() {
		List<PerfMetricVO> metrics = new ArrayList<PerfMetricVO>();
		PerfMetricVO perfMetricVO = new PerfMetricVO();
		perfMetricVO.setHost("host");
		perfMetricVO.setHostId("01a31fc518c44166afe29a8694f4b3e8");
		perfMetricVO.setTimestamp((long) 1491667200000L);
		perfMetricVO.setMetric("metric.metric.metric");
		metrics.add(perfMetricVO);
		HttpServletRequest request = new HttpServletTest();
		as.oldIntakePerfMetric(metrics, request);
	}

	@Test
	public void testOldGetPerfMetricList() {
		HttpServletRequest request = new HttpServletTest();
		request.getAttribute(GatewayConstants.TENANT_ID);
		SeriesRequestParam param = new SeriesRequestParam();
		param.setMetric("system.cpu.idle");
		long to = System.currentTimeMillis();
		long from = to - 30 * 60 * 1000;
		param.setFrom(from + 300 * 60 * 1000);
		param.setInterval(10);
		try {
			as.oldGetPerfMetricList(param, request);
		} catch (Exception e) {
			param.setTo(to);
			param.setFrom(from - 91 * 24 * 60 * 60L * 1000L);
			try {
				as.oldGetPerfMetricList(param, request);
			} catch (Exception e1) {
				param.setFrom(from);
				param.setAggregator("avg");
				as.oldGetPerfMetricList(param, request);
				GroupBy gb = new GroupBy();
				gb.setTagKey("host");
				gb.setAggregator("max");
				param.setGroupBy(gb);
				as.oldGetPerfMetricList(param, request);
				List<String> tags = new ArrayList<>();
				tags.add("hey:hey");
				param.setTags(tags);
				as.oldGetPerfMetricList(param, request);
			}
		}

	}

	//@Test
	public void testOldIntakeEvent() {
		HttpServletRequest request = new HttpServletTest();
		request.getAttribute(GatewayConstants.TENANT_ID);
		List<EventVO> list = new ArrayList<>();
		as.oldIntakeEvent(list, request);
		EventVO e = new EventVO("01a31fc518c44166afe29a8694f4b3e8", "01a31fc518c44166afe29a8694f4b3e8", "cpu_event",
				"CPU异常报警", System.currentTimeMillis(), "", "warning", new ArrayList<String>(), "sysLog");
		list.add(e);
		List<String> tags = new ArrayList<>();
		tags.add("hey:hey");
		e.setTags(tags);
		as.oldIntakeEvent(list, request);
	}

	@Test
	public void testOldIntakeHost() {
		HttpServletRequest request = new HttpServletTest();
		request.getAttribute(GatewayConstants.TENANT_ID);
		List<HostVO> list = new ArrayList<>();
		as.intakeHost(list, request);
		List<String> tags = new ArrayList<>();
		tags.add("hey:hey");
		List<String> apps = new ArrayList<>();
		apps.add("system");
		HostVO h = new HostVO("01a31fc518c44166afe29a8694f4b3e8", "WINPC",
				"10.1.10.22", "1", new Date(), tags, apps, "", true);
		list.add(h);
		as.oldIntakeHost(list, request);
	}

	@Test
	public void testOldGetEvents() {
		HttpServletRequest request = new HttpServletTest();
		/*String endTime = "2016-12";
		String beginTime = "2016-10";*/
		long beginTime = 1473664527000L;
        long endTime = 1473668127000L;
		as.oldGetEvents(request, 10, 10, "search_value", "severity", beginTime, endTime);
	}

	@Test
	public void testOldGetHostById() {
		HttpServletRequest request = new HttpServletTest();
		as.oldGetHostById(request, "123");
	}

	@Test
	public void testOldGetHosts() {
		HttpServletRequest request = new HttpServletTest();
		BatchHostRequestParam param = new BatchHostRequestParam();
		Date minUpdateTime = new Date();
		param.setMinUpdateTime(minUpdateTime);
		as.oldGetHosts(request, 10, 10, param);
	}

	@Test
	public void testOldGetMetricSnapshoot() {
		HttpServletRequest request = new HttpServletTest();
		as.oldGetMetricSnapshoot(request, ID, "metricName", "groupBy");
	}

	@Test
	public void testOldIntakeCheckPoints() {
		HttpServletRequest request = new HttpServletTest();
		request.getAttribute(GatewayConstants.TENANT_ID);
		List<CheckpointVO> list = new ArrayList<>();
		as.intakeCheckPoints(list, request);
		String[] tags = { "location:a", "level:high" };
		CheckpointVO c = new CheckpointVO("147707b79b79bd00a55d876efe54d0ef", "onlineState", tags,
				System.currentTimeMillis(), "ok");
		list.add(c);
		as.oldIntakeCheckPoints(list, request);
	}

	@Test
	public void testOldGetStateSnapshoot() {
		HttpServletRequest request = new HttpServletTest();
		request.getAttribute(GatewayConstants.TENANT_ID);
		as.oldGetStateSnapshoot(request, "fe01ce2a7fbac8fafaed7c982a04e229", "OnlineState");
	}

	@Test
	public void testOldGetStateHistory() {
		String[] tags = { "location:a", "level:high" };
		long lastTime = System.currentTimeMillis();
		long firstTime = lastTime - 1000 * 60 * 30L;
		HttpServletRequest request = new HttpServletTest();
		request.getAttribute(GatewayConstants.TENANT_ID);
		as.oldGetStateHistory(request, "OnlineState", "fe01ce2a7fbac8fafaed7c982a04e229", tags, firstTime, lastTime);
	}

	/******************************************************************************
	 * 以下是按新规范修改后的接口测试
	 ******************************************************************************/

	@Test
	public void testIntakePerfMetric() {
		List<PerfMetricVO1> metrics = new ArrayList<PerfMetricVO1>();
		PerfMetricVO1 perfMetricVO = new PerfMetricVO1();
		perfMetricVO.setHost("host");
		perfMetricVO.setHost_id("fe01ce2a7fbac8fafaed7c982a04e229");
		perfMetricVO.setTimestamp((long) 1491667200000L);
		perfMetricVO.setMetric("metric.metric.metric");
		metrics.add(perfMetricVO);
		HttpServletRequest request = new HttpServletTest();
		as.intakePerfMetric(metrics, request);
	}

	@Test
	public void testGetPerfMetricList() {
		HttpServletRequest request = new HttpServletTest();
		request.getAttribute(GatewayConstants.TENANT_ID);
		SeriesRequestParam1 param = new SeriesRequestParam1();
		param.setMetric("system.cpu.idle");
		long to = System.currentTimeMillis();
		long from = to - 30 * 60 * 1000;
		param.setFrom(from + 300 * 60 * 1000);
		param.setInterval(10);
		try {
			as.getPerfMetricList(param, request);
		} catch (Exception e) {
			param.setTo(to);
			param.setFrom(from - 91 * 24 * 60 * 60L * 1000L);
			try {
				as.getPerfMetricList(param, request);
			} catch (Exception e1) {
				param.setFrom(from);
				param.setAggregator("avg");
				as.getPerfMetricList(param, request);
				GroupBy1 gb = new GroupBy1();
				gb.setTag_key("host");
				gb.setAggregator("max");
				param.setGroup_by(gb);
				as.getPerfMetricList(param, request);
				List<String> tags = new ArrayList<>();
				tags.add("hey:hey");
				param.setTags(tags);
				as.getPerfMetricList(param, request);
			}
		}

	}

	//@Test
	public void testIntakeEvent() {
		HttpServletRequest request = new HttpServletTest();
		request.getAttribute(GatewayConstants.TENANT_ID);
		List<EventVO1> list = new ArrayList<>();
		//as.intakeEvent(list, request);
		EventVO1 e = new EventVO1("01a31fc518c44166afe29a8694f4b3e8", "cpu_event",
				"CPU异常报警", System.currentTimeMillis(), "", "warning", new ArrayList<String>(), "sysLog");
		list.add(e);
		List<String> tags = new ArrayList<>();
		tags.add("hey:hey");
		e.setTags(tags);
		as.intakeEvent(list, request);
	}

	@Test
	public void testIntakeHost() {
		HttpServletRequest request = new HttpServletTest();
		request.getAttribute(GatewayConstants.TENANT_ID);
		List<HostVO> list = new ArrayList<>();
		as.intakeHost(list, request);
		List<String> tags = new ArrayList<>();
		tags.add("hey:hey");
		List<String> apps = new ArrayList<>();
		apps.add("system");
		HostVO h = new HostVO("01a31fc518c44166afe29a8694f4b3e8", "WINPC",
				"10.1.10.22", "1", new Date(), tags, apps, "", true);
		list.add(h);
		as.intakeHost(list, request);
	}

	@Test
	public void testGetEvents() {
		HttpServletRequest request = new HttpServletTest();
		/*String endTime = "2016-12";
		String beginTime = "2016-10";*/
		long beginTime = 1473664527000L;
        long endTime = 1473668127000L;
		as.getEvents(request, 10, 10, "search_value", "severity", beginTime, endTime);
	}

	@Test
	public void testGetHostById() {
		HttpServletRequest request = new HttpServletTest();
		as.getHostById(request, "123");
	}

	@Test
	public void testGetPageHosts() {
		HttpServletRequest request = new HttpServletTest();
		as.getPageHosts(request, 10, 10, null, null, null, null, null, null);
	}

	@Test
	public void testGetHosts() {
		HttpServletRequest request = new HttpServletTest();
		as.getHosts(request, null, null, null, null, null, null);
	}

	@Test
	public void testGetMetricSnapshoot() {
		HttpServletRequest request = new HttpServletTest();
		as.getMetricSnapshoot(request, ID, "metricName", "groupBy");
	}

	@Test
	public void testIntakeCheckPoints() {
		HttpServletRequest request = new HttpServletTest();
		request.getAttribute(GatewayConstants.TENANT_ID);
		List<CheckpointVO> list = new ArrayList<>();
		as.intakeCheckPoints(list, request);
		String[] tags = { "location:a", "level:high" };
		CheckpointVO c = new CheckpointVO("147707b79b79bd00a55d876efe54d0ef", "onlineState", tags,
				System.currentTimeMillis(), "ok");
		list.add(c);
		as.intakeCheckPoints(list, request);
	}

	@Test
	public void testGetStateSnapshoot() {
		HttpServletRequest request = new HttpServletTest();
		request.getAttribute(GatewayConstants.TENANT_ID);
		as.getStateSnapshoot(request, "fe01ce2a7fbac8fafaed7c982a04e229", "OnlineState");
	}

	@Test
	public void testGetStateHistory() {
		String[] tags = { "location:a", "level:high" };
		long lastTime = System.currentTimeMillis();
		long firstTime = lastTime - 1000 * 60 * 30L;
		HttpServletRequest request = new HttpServletTest();
		request.getAttribute(GatewayConstants.TENANT_ID);
		as.getStateHistory(request, "OnlineState", "fe01ce2a7fbac8fafaed7c982a04e229", tags, firstTime, lastTime);
	}

	@Test
	public void testIntakeMetricMetaData() {
		HttpServletRequest request = new HttpServletTest();
		request.getAttribute(GatewayConstants.TENANT_ID);
		List<MetricMetaDataVO> list = new ArrayList<>();
		as.intakeMetricMetaData(list, request);
		MetricMetaDataVO m = new MetricMetaDataVO("test.cpu.usage", "mbps", 1d, 100d, 2, "gauge", "", "", "test");
		list.add(m);
		as.intakeMetricMetaData(list, request);
	}
}
