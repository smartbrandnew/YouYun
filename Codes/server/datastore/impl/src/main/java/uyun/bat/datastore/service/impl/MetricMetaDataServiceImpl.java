package uyun.bat.datastore.service.impl;

import java.util.List;

import javax.annotation.Resource;

import uyun.bat.datastore.api.entity.MetricMetaData;
import uyun.bat.datastore.api.service.MetricMetaDataService;
import uyun.bat.datastore.dao.MetricMetaDataDao;

public class MetricMetaDataServiceImpl implements MetricMetaDataService {
	@Resource
	private MetricMetaDataDao metricMetaDataDao;

	@Override
	public boolean insert(MetricMetaData data) {
		return metricMetaDataDao.insert(data) == 1;
	}

	@Override
	public boolean update(MetricMetaData data) {
		return metricMetaDataDao.update(data) == 1;
	}

	@Override
	public boolean delete(String name) {
		metricMetaDataDao.delete(name);
		return true;
	}

	@Override
	public MetricMetaData queryByName(String metricName) {
		return metricMetaDataDao.queryByName(metricName);
	}

	@Override
	public List<MetricMetaData> queryAll(String tenantId) {
		return metricMetaDataDao.queryAll(tenantId);
	}

	@Override
	public List<MetricMetaData> getMetricMetaDataByKey(String key) {
		return metricMetaDataDao.getMetricMetaDataByKey(key);
	}

	@Override
	public List<String> getAllMetricMetaDataName() {
		return  metricMetaDataDao.getAllMetricMetaDataName();
	}

	public List<MetricMetaData> getMetricsUnitByList(List<String> metricNames) {
		return metricMetaDataDao.getMetricsUnitByList(metricNames);
	}

	@Override
	/**
	 * 查询有最小最大值的指标元数据列表
	 */
	public List<MetricMetaData> queryRangedMetaData(String tenantId) {
		return metricMetaDataDao.queryRangedMetaData(tenantId);
	}
}
