package uyun.bat.datastore.dao;

import java.util.List;
import java.util.Map;

import uyun.bat.datastore.api.entity.ResourceCount;
import uyun.bat.datastore.api.entity.ResourceStatusCount;
import uyun.bat.datastore.api.entity.SimpleResource;
import uyun.bat.datastore.entity.MetricSpanTime;
import uyun.bat.datastore.entity.SimpleResourceQuery;

public interface SimpleResourceDao {
	List<ResourceCount> getResCountByDate(Map<String, Object> map);

	List<ResourceCount> getResCount();

	List<ResourceStatusCount> getResStatusCount(String tenantId);

	List<String> getResIdInId(List<String> list);

	List<String> getAllTenantId();

	int getResCountByTenantId(String tenantId);

	List<MetricSpanTime> getMetricSpanTime();

	List<String> getAllResId(String tenantId);

	List<SimpleResource> getSimpleResource(SimpleResourceQuery query);

	long batchInsert(List<SimpleResource> list);

	long batchUpdate(List<SimpleResource> list);

	int save(SimpleResource simpleResource);

	int delete(String tenantId, String resourceId);

	long batchDelete(Map<String, Object> map);
}
