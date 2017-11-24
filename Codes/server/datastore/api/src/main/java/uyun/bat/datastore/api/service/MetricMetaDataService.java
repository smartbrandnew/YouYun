package uyun.bat.datastore.api.service;

import java.util.List;

import uyun.bat.datastore.api.entity.MetricMetaData;

public interface MetricMetaDataService {
	/**
	 * 插入指标元数据
	 * @param data
	 * @return
	 */
	boolean insert(MetricMetaData data);

	/**
	 * 更新指标元数据
	 * @param data
	 * @return
	 */
	boolean update(MetricMetaData data);

	/**
	 * 删除指标元数据
	 * @param metricName
	 * @return
	 */
	boolean delete(String metricName);

	/**
	 * 根据指标名称查询元数据
	 * @param metricName
	 * @return
	 */
	MetricMetaData queryByName(String metricName);

	/**
	 * 查询所有元数据信息
	 * @return
	 */
	List<MetricMetaData> queryAll(String tenantId);
	
	/**
	 * 通过中间件名称查询所有元数据信息
	 * @return
	 */
	List<MetricMetaData> getMetricMetaDataByKey(String key);

	/**
	 * 查询所有的指标元数据名
	 * @return
	 */
	List<String> getAllMetricMetaDataName();

	List<MetricMetaData> getMetricsUnitByList(List<String> metricNames);

	/**
	 * 获取有最大最小值的元数据
	 * @param tenantId
	 * @return
	 */
	List<MetricMetaData> queryRangedMetaData(String tenantId);
}
