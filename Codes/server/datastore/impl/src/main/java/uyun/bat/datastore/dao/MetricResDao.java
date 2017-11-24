package uyun.bat.datastore.dao;

import java.util.List;
import java.util.Map;

import uyun.bat.datastore.api.serviceapi.entity.ResourceServiceQuery;
import uyun.bat.datastore.entity.ResourceMetrtics;

public interface MetricResDao {

	ResourceMetrtics getMetricNamesByResId(String resourceId);

	boolean update(ResourceMetrtics resMetrics);

	boolean delete(String id);

	boolean insert(ResourceMetrtics resMetrics);

	List<ResourceMetrtics> getMetricNamesByTenantId(String tenantId);

	long batchUpdate(List<ResourceMetrtics> resMetrics);

	long batchInsert(List<ResourceMetrtics> resMetrics);

	long batchDelete(List<String> ids);

	List<String> getResIdInId(List<String> list);

	Map<String, List<String>> getMetricNames(ResourceServiceQuery query);
	
	List<ResourceMetrtics> getMetricNamesInResId(Map<String, Object> map);
}
