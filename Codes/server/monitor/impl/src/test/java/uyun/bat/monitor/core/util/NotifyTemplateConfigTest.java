package uyun.bat.monitor.core.util;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import uyun.bat.datastore.api.entity.MetricMetaData;
import uyun.bat.datastore.api.service.MetricMetaDataService;
import uyun.bat.event.api.entity.Event;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.core.entity.CheckContext;
import uyun.bat.monitor.core.entity.MonitorParam;
import uyun.bat.monitor.core.logic.ConstantDef;
import uyun.bat.monitor.impl.common.ServiceManager;

public class NotifyTemplateConfigTest {

	MetricMetaDataService metaDataService = new MetricMetaDataService() {

		@Override
		public boolean update(MetricMetaData data) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public MetricMetaData queryByName(String metricName) {
			MetricMetaData metaData = new MetricMetaData();
			metaData.setUnit(metricName);
			return metaData;
		}

		@Override
		public List<MetricMetaData> queryAll(String tenantId) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean insert(MetricMetaData data) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public List<MetricMetaData> getMetricMetaDataByKey(String key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<String> getAllMetricMetaDataName() {
			return null;
		}

		@Override
		public List<MetricMetaData> getMetricsUnitByList(List<String> metricNames) {
			return null;
		}

		@Override
		public List<MetricMetaData> queryRangedMetaData(String tenantId) {
			return null;
		}

		@Override
		public boolean delete(String metricName) {
			// TODO Auto-generated method stub
			return false;
		}

	};
	@Test
	public void testGenerateEvent() {
		MonitorParam monitorParam = new MonitorParam() {
			
			@Override
			public Map<String, String> getParamMap() {
				// TODO Auto-generated method stub
				Map<String,String>map = new HashMap<String, String>();
				map.put("${metric}","test");
				map.put("${duration_warn}","test");
				map.put("${aggregator}","test");
				map.put("${threshold_warn_unit}","指标监测器阈值单位");
				map.put("${threshold_unit}", "阈值单位");  
				return map;
			}
		};
		ServiceManager.getInstance().setMetricMetaDataService(metaDataService);
		Event event = new Event();
		MonitorState monitorState = MonitorState.OK;
		event.setMonitorType("metric");
		CheckContext context = new CheckContext();
		context.setEvent(event);
		context.setHostName(ConstantDef.HOST_NAME);
		context.setIp(ConstantDef.TEST_ID);
		context.setValue(ConstantDef.TEST_VALUE);
		context.setMonitorParam(monitorParam);
		context.setMonitorState(monitorState);
		context.setLastMonitorState(MonitorState.WARNING);
		Event eventString = NotifyTemplateConfig.getInstance().generateEvent(context);
		System.out.println(eventString.toString());
		
		//generateSMS()方法测试
		Date occurTime = new Date();
		event.setOccurTime(occurTime);
		String smsString = NotifyTemplateConfig.getInstance().generateSMS(context);
		System.out.println(smsString);
		//generateEmailTitle()方法测试
		String etString = NotifyTemplateConfig.getInstance().generateEmailTitle(context);
		System.out.println(etString);
		//generateEmailContent()方法测试
		String ecString = NotifyTemplateConfig.getInstance().generateEmailContent(context);
		System.out.println(ecString);
	}

	@Test
	public void init(){
		NotifyTemplateConfig  n = NotifyTemplateConfig.getInstance();
	}
}
