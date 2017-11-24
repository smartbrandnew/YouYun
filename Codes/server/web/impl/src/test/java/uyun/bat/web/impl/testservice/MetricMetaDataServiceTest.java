package uyun.bat.web.impl.testservice;

import java.util.ArrayList;
import java.util.List;

import uyun.bat.datastore.api.entity.MetricMetaData;
import uyun.bat.datastore.api.entity.MetricType;
import uyun.bat.datastore.api.service.MetricMetaDataService;

public class MetricMetaDataServiceTest implements MetricMetaDataService{

	@Override
	public boolean insert(MetricMetaData data) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean update(MetricMetaData data) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete(String metricName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MetricMetaData queryByName(String metricName) {
		// TODO Auto-generated method stub
		MetricMetaData m=new MetricMetaData();
		m.setName("system.load.5");
		m.setUnit(null);
		m.setValueMax(null);
		m.setValueMin(null);
		m.setPrecision(0);
		m.setType(MetricType.gauge);
		m.setcName("过去5分钟内系统负载");
		m.setcDescr("");
		return m;
	}

	@Override
	public List<MetricMetaData> queryAll(String tenantId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MetricMetaData> getMetricMetaDataByKey(String key) {
		// TODO Auto-generated method stub
		List<MetricMetaData> list = new ArrayList<MetricMetaData>();
		MetricMetaData m=new MetricMetaData();
		m.setName("system.load.5");
		m.setUnit(null);
		m.setValueMax(null);
		m.setValueMin(null);
		m.setPrecision(0);
		m.setType(MetricType.gauge);
		m.setcName("过去5分钟内系统负载");
		m.setcDescr("");
		list.add(m);
		return list;
	}

	@Override
	public List<String> getAllMetricMetaDataName() {
		List<String> list = new ArrayList<>();
		list.add("system.cpu.idle");
		list.add("system.cpu.pct_usage");
		return list;
	}

	public List<MetricMetaData> getMetricsUnitByList(List<String> metricNames) {
		return null;
	}

	@Override
	public List<MetricMetaData> queryRangedMetaData(String tenantId) {
		return new ArrayList<MetricMetaData>();
	}
}
