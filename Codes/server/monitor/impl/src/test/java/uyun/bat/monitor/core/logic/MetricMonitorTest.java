package uyun.bat.monitor.core.logic;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;
import uyun.bat.monitor.api.common.util.PeriodUtil.Period;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.Options;
import uyun.bat.monitor.core.entity.Aggregator;
import uyun.bat.monitor.core.entity.MetricData;
import uyun.bat.monitor.core.entity.MetricMonitorParam;
import uyun.bat.monitor.impl.common.ServiceManager;
import uyun.bat.monitor.core.entity.TagEntry;

public class MetricMonitorTest {
	//private static ServiceManager serviceManager = Startup.getInstance().getBean(ServiceManager.class);

	Monitor monitor = new Monitor();
	MetricMonitorParam metricMonitorParam = new MetricMonitorParam(); 
	MetricMonitor metriMonitor = new MetricMonitor(monitor, metricMonitorParam);
	ArrayList<TagEntry> tags = new ArrayList<TagEntry>();
	TagEntry tagEntry = new TagEntry();
	MonitorState monitorState = MonitorState.OK;
	
	@Test
	public void testMatch() {
		tagEntry.setKey(ConstantDef.TEST_KEY);
		tagEntry.setValue(ConstantDef.TEST_VALUE);
		tags.add(tagEntry);
		MetricData data = new MetricData();
		data.setMetric(ConstantDef.TEST_METRIC);
		data.setTags(tags);
		metricMonitorParam.setMetric(ConstantDef.TEST_METRIC);
		boolean b = metriMonitor.match(data);
		System.out.println(b);
	}
	
	@Test
	public void testCheckIfMonitorStatusRollover(){
		
		ServiceManager.getInstance().setMetricService(InstantiateService.metricService);
		Options options = new Options();
		HashMap<String,String> map = new HashMap<String,String>();
		Aggregator aggregator = Aggregator.MAX;
		map.put("alert", "last_5m < 8");
		options.setThresholds(map);
		metricMonitorParam.setThresholds(options);
		Period period = new Period();
		period.setStart(123);
		period.setEnd(234);
		monitor.setTenantId(ConstantDef.TENANT_ID);
		metricMonitorParam.setMetric(ConstantDef.TEST_METRIC);
		metricMonitorParam.setAggregator(aggregator);
		monitorState.setValue((short)1);
		monitorState = metriMonitor.checkIfMonitorStatusRollover();
		System.out.println(monitorState);
		metriMonitor.doAfterCheck();
	}

}
