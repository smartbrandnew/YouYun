package uyun.bat.datastore.dao;

import java.util.List;

import uyun.bat.datastore.api.entity.MetricMetaData;

public interface MetricMetaDataDao {

	int insert(MetricMetaData data);

	int update(MetricMetaData data);

	int delete(String name);

	MetricMetaData queryByName(String metricName);

	List<MetricMetaData> queryAll(String tenantId);
	
	List<String> getMetricNames(int limit);
	
	List<String> getMetricNamesByKey(String key, int limit);
	
	List<MetricMetaData> getMetricMetaDataByKey(String key);

	List<String> getAllMetricMetaDataName();

	List<MetricMetaData> getMetricsUnitByList(List<String> metricNames);

	List<MetricMetaData> queryRangedMetaData(String tenantId);
}
