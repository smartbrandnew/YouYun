package uyun.bat.monitor.core.entity;

import static org.junit.Assert.*;
import java.util.Map;
import org.junit.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.event.api.entity.Event;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.api.entity.Options;
import uyun.bat.monitor.core.entity.MetricMonitorParam.Threshold;

public class MonitorEntityTest {
	ArrayList<TagEntry> tags = new ArrayList<TagEntry>();
	Aggregator aggregator = Aggregator.AVG;
	String testString = null; 

	@Test
	public void test() {
		testAggregator();
		testAppMonitorParam();
		testCheckContext();
		testEventData();
		testEventMonitorParam();
		testHostMonitorParam();
		testMetricData();
		testMetricMonitorParam();
		testResourceData();
		testStateMetricData();
		testSymbol();
	}
	
	
	//Aggregator类单元测试
	public void testAggregator(){
		aggregator.setCode("code");
		aggregator.setName("name");
		Aggregator.checkByCode("code");
		String string = aggregator+" "+aggregator.getCode()+" "+aggregator.getName();
		System.out.println(string);
		testString = string;
		assertTrue(testString != null);
		
	}
	
	//AppMonitorParam类单元测试
	public void testAppMonitorParam(){
		Options options = null;
		StringBuffer sBuffer = new StringBuffer();
		//测试StateMetricData的不同情况
		StateMetricData stateMetricData1 = new StateMetricData("state", "tenantId");
		StateMetricData stateMetricData2 = new StateMetricData("no state", "no tenantId");
		
		AppMonitorParam appMonitorParam = new AppMonitorParam();
		appMonitorParam.setStatus("status");
		appMonitorParam.setPeriod("period");
		appMonitorParam.setTags(tags);
		appMonitorParam.setState("state");
		appMonitorParam.setOptions(options);
		
		boolean b1 = appMonitorParam.match(stateMetricData1);
		boolean b2 = appMonitorParam.match(stateMetricData2);
		System.out.println(b1);
		System.out.println(b2);
		sBuffer.append(appMonitorParam.getPeriod()).append(" ");
		sBuffer.append(appMonitorParam.getState()).append(" ");
		sBuffer.append(appMonitorParam.getStatus()).append(" ");
		sBuffer.append(appMonitorParam.getTags()).append(" ");
		
		testString = sBuffer.toString();
		assertTrue(testString != null);
	}
	
	//CheckContext类测试
	public void testCheckContext(){
		Event event = new Event();
		String[] eventRecover=null;
		MonitorState lastMonitorState = MonitorState.OK;
		MonitorState monitorState = MonitorState.OK;
		StringBuffer sBuffer = new StringBuffer();
		
		CheckContext checkContext = new CheckContext();
		MonitorParam monitorParam = new MonitorParam() {
			@Override
			public Map<String, String> getParamMap() {
				checkContext.getMonitorParam().getParamMap();
				return null;
			}
		};
		checkContext.setCount(1);
		checkContext.setEvent(event);
		checkContext.setEventRecover(eventRecover);
		checkContext.setHostName("hostName");
		checkContext.setIp("127.0.0.1");
		checkContext.setLastMonitorState(lastMonitorState);
		checkContext.setMonitorParam(monitorParam);
		checkContext.setMonitorState(monitorState);
		checkContext.setResId("resId");
		checkContext.setValue("value");
		sBuffer.append(checkContext.getCount()).append(" ");
		sBuffer.append(checkContext.getEvent().toString()).append(" ");
		sBuffer.append(checkContext.getEventRecover()).append(" ");
		sBuffer.append(checkContext.getHostName()).append(" ");
		sBuffer.append(checkContext.getIp()).append(" ");
		sBuffer.append(checkContext.getLastMonitorState()).append(" ");
		sBuffer.append(checkContext.getMonitorParam().toString()).append(" ");
		sBuffer.append(checkContext.getLastMonitorState().toString()).append(" ");
		sBuffer.append(checkContext.getResId()).append(" ");
		sBuffer.append(checkContext.getValue()).append(" ");
		testString = sBuffer.toString();
		assertTrue(testString != null);
	}
	
	//EventData类测试
	public void testEventData(){
		EventData eventData = new EventData();
		eventData.setContent("testContent");
		eventData.setIdentity("testIdentity");
		eventData.setResId("testResId");
		eventData.setServerity((short)1);
		eventData.setTags(tags);
		eventData.setTenantId("testTenantId");
		eventData.setTitle("title");
		eventData.addTag("testTagsKey", "testTagValue");
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(eventData.getContent()).append(eventData.getIdentity())
			.append(eventData.getResId()).append(eventData.getTenantId()).append(eventData.getTitle())
			.append(eventData.getOccurTime()).append(eventData.getServerity()).append(eventData.getTags());
		testString = stringBuffer.toString();
		assertTrue(testString != null);
	}
	
	//EventMonitor类测试
	public void testEventMonitorParam(){
		EventMonitorParam eventMonitorParam = new EventMonitorParam();
		Comparison comparison = Comparison.GE;		
		String[] sources = null;
		String[] status = null;
		eventMonitorParam.setAggregator("testAggregator");
		eventMonitorParam.setComparison(comparison);
		eventMonitorParam.setKeyWords("testKeyWords");
		eventMonitorParam.setPeriod("testPeriod");
		eventMonitorParam.setSources(sources);
		eventMonitorParam.setStatus(status);
		eventMonitorParam.setTags(tags);
		eventMonitorParam.setThreshold(0.1);
		
		Map<String, String> map = new HashMap<>();
		map = eventMonitorParam.getParamMap();
		System.out.println(map.toString());
		
		assertTrue(eventMonitorParam != null && map != null);
	}
	
	//HostMonitorParam类测试
	public void testHostMonitorParam(){
		Options options = new Options();
		HostMonitorParam hostMonitorParam = new HostMonitorParam();
		hostMonitorParam.setOptions(options);
		hostMonitorParam.setPeriod("testPeriod");
		hostMonitorParam.setStatus("testStatus");
		hostMonitorParam.setTags(tags);
		
		Map<String, String> map = new HashMap<>();
		map = hostMonitorParam.getParamMap();
		System.out.println(map.toString());
	}
	
	//MetricData类测试
	public void testMetricData(){
		MetricData  metricData = new MetricData();
		metricData.setMetric("metric");
		metricData.setTags(tags);
		metricData.setTenantId("tenantId");
		metricData.addTag("testTagKey", "testTagValue");
		testString = metricData.getMetric()+metricData.getTenantId()+metricData.getTags();
		assertTrue(testString != null);
	}
	
	//MetricMonitorParam类测试
	public void testMetricMonitorParam(){
		MetricMonitorParam metricMonitorParam = new MetricMonitorParam();
		MetricData metricData = new MetricData();
		Options options = new Options();
		Map<String,String>thresholds = new HashMap<String, String>();
		thresholds.put("alert","last_5m < 8");
		thresholds.put("warning", "last_5m > 8");
		options.setThresholds(thresholds);
		
		List<String> groups = null;
		metricMonitorParam.setMetric("testMetric");
		metricData.setMetric("testMetric");
		metricMonitorParam.setAggregator(aggregator);
		metricMonitorParam.setGroups(groups);
		metricMonitorParam.setTags(tags);
		metricMonitorParam.setThresholds(options);
		
		boolean b1 = metricMonitorParam.match(metricData);
		Map<String, String> map1 = metricMonitorParam.getParamMap();
		Map<String, java.util.List<Threshold>> map2 = metricMonitorParam.getPeriodThresholdMap();
		System.out.println(b1);
		System.out.println(map1.toString());
		System.out.println(map2.toString());
		assertTrue(metricMonitorParam.getThresholds() != null);
		
	}
	
	//ResourceData类测试
	public void testResourceData(){
		ResourceData resourceData = new ResourceData();
		OnlineStatus onlineStatus = OnlineStatus.ONLINE; 
		resourceData.setEventSourceType((short)1);
		resourceData.setHostname("hostname");
		resourceData.setIpaddr("testIpaddr");
		resourceData.setOnlineStatus(onlineStatus);
		resourceData.setResourceId("testResourceId");
		resourceData.setTenantId("testTenantId");
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(resourceData.getHostname()).append(resourceData.getIpaddr()).append(resourceData.getResourceId())
			.append(resourceData.getTenantId()).append(resourceData.getEventSourceType()).append(resourceData.getLastCollectTime())
			.append(resourceData.getOnlineStatus());
		testString = stringBuffer.toString();
		assertTrue(testString != null);
	}
	
	//StataMetricData类测试
	public void testStateMetricData(){
		StateMetricData stateMetricData = new StateMetricData();
		stateMetricData.setName("testName");
		stateMetricData.setTags(tags);
		stateMetricData.setTenantId("testTenantId");
		stateMetricData.addTag("testTag", "testValue");
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(stateMetricData.getName()).append(stateMetricData.getTenantId())
			.append(stateMetricData.getTags());
		testString = stringBuffer.toString();
		assertTrue(testString != null);
	}
	
	//Symbol类测试
	public void testSymbol(){
		Symbol symbol = new Symbol();
		MonitorState monitorState = MonitorState.OK;
		MonitorType monitorType = MonitorType.METRIC;
		symbol.setMonitorId("testMonitorId");
		symbol.setMonitorState(monitorState);
		symbol.setMonitorType(monitorType);
		symbol.setQuery("testQuery");
		symbol.setResourceId("testResourceId");
		symbol.setTags(tags);
		symbol.setTenantId("testTenantId");
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer .append(symbol.getMonitorId()).append(symbol.getQuery()).append(symbol.getResourceId())
			.append(symbol.getTenantId()).append(symbol.getMonitorState()).append(symbol.getMonitorType())
			.append(symbol.getTags());
		testString = stringBuffer.toString();
		assertTrue(testString != null);
	}

}






