package uyun.bat.gateway.dd_agent.service.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.easymock.EasyMock;
import org.junit.Test;

import uyun.bat.gateway.api.service.ServiceManager;
import uyun.bat.gateway.dd_agent.CustomMetricServiceTest;
import uyun.bat.gateway.dd_agent.entity.DDAgentData;
import uyun.bat.gateway.dd_agent.entity.DDMetric;
import uyun.bat.gateway.dd_agent.entity.DDSeries;
import uyun.bat.gateway.dd_agent.entity.DDSeriesMetric;
import uyun.bat.gateway.dd_agent.entity.DDServiceCheck;
import uyun.bat.gateway.dd_agent.entity.NetDevData;
import uyun.bat.gateway.dd_agent.entity.NetEquipment;
import uyun.bat.gateway.dd_agent.entity.TagEntry;

public class DD_AgentRESTServiceTest {
	DD_AgentRESTService dd=new DD_AgentRESTService();
	HttpServletRequest request =EasyMock.createMock(HttpServletRequest.class);
	{
		ServiceManager.getInstance().setCustomMetricService(new CustomMetricServiceTest());
		EasyMock.expect(request.getAttribute("_tenant_id_")).andReturn("gatewayTest");
		EasyMock.replay(request);
	}
	
	@Test
	public void testIntake() {
		DDAgentData data=new DDAgentData();
		data.setTimestamp(new Date().getTime()-1);
		Map<String, List<TagEntry>> hostTags=new HashMap<String, List<TagEntry>>();
		List<TagEntry> entry=new ArrayList<TagEntry>();
		TagEntry tagEntry=new TagEntry();
		tagEntry.setKey("uyun");
		tagEntry.setValue("monitor");
		entry.add(tagEntry);
		hostTags.put("system", entry);
		data.setHostTags(hostTags);
		List<DDMetric> metrics=new ArrayList<DDMetric>();
		DDMetric d=new DDMetric();
		DDMetric mm=new DDMetric();
		d.setMetric("datadog-uyun");
		d.setHostName("datadog-hostname");
		mm.setMetric("monitor-uyun");
		mm.setHostName("monitor-hostname");
		metrics.add(d);
		metrics.add(mm);
		data.setMetrics(metrics);
		data.setGohai("[{ip:123}]");
		
		dd.intake(data, request);
	}

	@Test
	public void testIntakeMetrics() {
		//TODO
		/*HttpServletRequest request=new HttpServletTest();
		dd.intakeMetrics("uyun:monitor", request);*/
	}

	@Test
	public void testIntakeMedata() {
		//TODO
		/*HttpServletRequest request=new HttpServletTest();
		dd.intakeMedata("uyun:monitor", request);*/
	}

	@Test
	public void testSeries() {
		DDSeries series=new DDSeries();
		series.setUuid("123");
		List<DDSeriesMetric> list=new ArrayList<DDSeriesMetric>();
		DDSeriesMetric ddsm=new DDSeriesMetric();
		DDSeriesMetric ddsm2=new DDSeriesMetric();
		ddsm.setMetric("monitor:uyun");
		ddsm.setHost("主机1");
		List<TagEntry> tags=new ArrayList<TagEntry>();
		TagEntry t= new TagEntry();
		t.setKey("uyun");
		t.setValue("monitor");
		ddsm.setTags(tags);
		ddsm.setDeviceName("centos云主机");
		List<double[]> points=new ArrayList<double[]>();
		double[] e=new double[2];
		e[0]=System.currentTimeMillis();
		e[1]=System.currentTimeMillis()+10;
		points.add(e);
		ddsm.setPoints(points);
		ddsm2.setMetric("datadog:system");
		ddsm2.setHost("主机2");
		list.add(ddsm);
		list.add(ddsm2);
		series.setSeries(list);
		
		dd.series(series, request);
	}

	@Test
	public void testCheck_run() {
		List<DDServiceCheck> checks=new ArrayList<DDServiceCheck>();
		DDServiceCheck c=new DDServiceCheck();
		
		checks.add(c);
		//暂时不插入指标状态
		dd.check_run(checks, request);
	}

	@Test
	public void testPing() {
		NetDevData data=new NetDevData();
		data.setTimestamp(System.currentTimeMillis());
		NetEquipment nem=new NetEquipment();

		data.setNetEquipment(nem);

		dd.ping(data, request);
	}

	public void testGetCurrentTime() {
		dd.getCurrentTime(request);
	}

}
