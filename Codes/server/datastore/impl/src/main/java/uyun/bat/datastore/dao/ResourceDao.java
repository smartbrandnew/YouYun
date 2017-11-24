package uyun.bat.datastore.dao;

import uyun.bat.datastore.api.entity.*;
import uyun.bat.datastore.api.serviceapi.entity.ResourceServiceQuery;
import uyun.bat.datastore.entity.*;

import java.util.List;
import java.util.Map;

public interface ResourceDao {

	int insertResAppBatch(List<ResourceApp> apps);

	int insertResTagBatch(List<ResourceTag> tags);

	long insertResBatch(List<Resource> resources);

	int saveRes(Resource resource);

	int updateResTagBatch(List<ResourceTag> tags);

	int updateResTag(ResourceTag tag);

	long updateResBatch(List<Resource> resources);

	int deleteResTagById(String id);

	int deleteResAppById(String id);

	int deleteResById(String id);

	boolean deleteResAppBatch(List<String> resIds);

	boolean deleteResTagBatch(List<String> resIds);

	long deleteResBatch(List<String> resIds);

	int getCountByEmptyTag(FilterQuery query);
	int getOnlineCountByEmptyTag(FilterQuery query);
	Resource getResById(String id);

	Resource getResByAgentId(String agentId, String tenantId);

	List<Resource> getAllRes(String tenantId);

	List<Resource> getAllResNotContainNetwork(String tenantId);

	List<String> getAllResId(String tenantId);

	List<Resource> getAllResPage(Map<String, Object> map);

	List<Resource> getResByApp(String tenantId, String appName);

	List<Resource> getResByKey(FilterQuery query);
	
	List<Resource> getResByEmptyTag(FilterQuery query);

	List<Resource> getResInId(List<String> resourceIds);

	List<Resource> getResByKeyAndSort(FilterQuery query);

	List<ResourceTag> getResTags(String tenantId);

	List<ResourceTag> getTagsById(String id);

	List<String> getAllResTagNames(String tenantId);

	List<String> getAppNamesById(String id);

	List<String> getResIdByKey(FilterQuery query);

	List<String> getResIdGroupByTag(GroupQuery query);

	int getCountByKey(FilterQuery query);

	List<Resource> getSimpleResource(SimpleResourceQuery query);

	List<Resource> queryResListByCondition(ResourceOpenApiQuery query);

	int queryResCountByCondition(ResourceOpenApiQuery query);

	List<ResourceCount> getResCountByDate(Map<String, Object> map);

	List<ResourceCount> getResCount();

	List<ResourceCount> getResCountByOnlineStatus(Map<String, String> map);

	List<ResTagResult> getTagByFilter(Map<String, Object> map);

	List<ResTagResult> getResTagsByTag(Map<String, Object> map);

	List<ResTagResult> getAllTags(String tenantId);

	List<String> getExistsIdInId(List<String> ids);

	int getOnlineCount(String tenantId);

	int getResCountByTenantId(String tenantId);

	int getOnlineCountByFilter(FilterQuery query);

	List<ResourceStatusCount> getResStatusCount(String tenantId);

	List<String> getAllApps();

	List<String> getResIdInId(List<String> list);

	List<String> getAuthorizationResIds(Map<String, Object> map);

	List<String> getAllTenantId();

	List<MetricSpanTime> getMetricSpanTime();

	List<SimpleResource> getSimpleResByTenantIdAndTags(SimpleResourceQuery query);

	List<Resource> getAllResHostAndTags(String tenantId);

	List<String> getResIdByResServieQuery(ResourceServiceQuery query);

	/**
	 * 统一资源库ID生成目前关键属性只有IP，IP与资源ID做个映射
	 *
	 * @param tenanId
	 * @param ipaddr
	 * @return
	 */

	List<String> getResIdByIpaddr(String tenanId, String ipaddr,String type);

	ResFieldMappingResult getResIpaddrById(String tenantId, String id);

	/**
	 * 根据指标名称查询资源范围和资源id
	 * @param params
	 * @return
     */
	List<Map<String, String>> getResTagsByMetricName(Map<String, Object> params);

	/**
	 * 资源范围查询资源
	 * @param params
	 * {
	 *     tenantId
	 *     ---可选参数---
	 *     resTags:[]// resTags in
	 *     ipaddrs:[]// ipaddr in
	 *	   sortField // order
	 *	   sortOrder
	 * }
	 * @return
     */
	List<Resource> getResByCondition(Map<String, Object> params);

	int updateUserTagBatchByIpaddr(List<Resource> list);

}
