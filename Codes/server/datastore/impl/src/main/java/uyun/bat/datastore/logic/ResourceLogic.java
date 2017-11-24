package uyun.bat.datastore.logic;

import java.util.Date;
import java.util.List;
import java.util.Map;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.PageResource;
import uyun.bat.datastore.api.entity.PageResourceGroup;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceCount;
import uyun.bat.datastore.api.entity.ResourceDetail;
import uyun.bat.datastore.api.entity.ResourceOpenApiQuery;
import uyun.bat.datastore.api.entity.ResourceOrderBy;
import uyun.bat.datastore.api.entity.ResourceStatusCount;
import uyun.bat.datastore.api.entity.SimpleResource;
import uyun.bat.datastore.entity.MetricSpanTime;

public interface ResourceLogic {

	boolean delete(String tenantId,String resourceId);
	
	boolean save(Resource resource);


	List<Resource> queryAllRes(String tenantId, boolean isContainNetwork);

	List<Tag> queryResourceTags(String tenantId);

	PageResourceGroup queryByFilterAndGroupByTag(String tenantId, String filter, String groupBy, int pageNo, int size,
			OnlineStatus onlineStatus);

	List<String> queryResTagNames(String tenantId);

	Resource queryResByAgentId(String agentId, String tenantId);

	PageResource queryByKey(String tenantId, String key, int pageNo, int size, OnlineStatus onlineStatus);

	PageResource queryByKeyAndSortBy(String tenantId, String filter, ResourceOrderBy orderBy, int pageNo, int size,
			OnlineStatus onlineStatus);

	PageResource queryAllRes(String tenantId, int pageNo, int pageSize, OnlineStatus onlineStatus);

	List<SimpleResource> query(OnlineStatus onlineStatus, long lastCollectTime);

	/**
	 * 添加或者更新资源
	 * 
	 * @param resourceDetail
	 * @return
	 */
	boolean saveResourceDetail(ResourceDetail resourceDetail);

	/**
	 * 查询资源详情
	 * 
	 * @param resId
	 * @return
	 */
	ResourceDetail queryByResId(String resId);

	/**
	 * 删除资源详情
	 * 
	 * @param resId
	 * @return
	 */
	boolean deleteResourceDetail(String resId);

	/**
	 * OpenApi查询资源列表
	 * 
	 * @param query
	 * @return
	 */
	PageResource queryResListByCondition(ResourceOpenApiQuery query);

	/********************************* 以下接口for门户 *******************************************/
	/**
	 * 查询租户某段时间内创建资源的数量
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	List<ResourceCount> getResCountByDate(Date startTime, Date endTime);

	/**
	 * 查询租户的全部资源数量
	 * 
	 * @return
	 */
	List<ResourceCount> getResCount();

	/**
	 * 根据tags获取tag标签,以分号";"作为分割条件
	 * 
	 * @param tenantId
	 * @param key
	 * @return
	 */
	List<String> getResTagsByTag(String tenantId, String tags);

	/**
	 * 根据资源在线离线总数
	 * 
	 * @param tenantId
	 * @return
	 */
	List<ResourceStatusCount> getResStatusCount(String tenantId);
	List<ResourceCount> getResCountByOnlineStatus(OnlineStatus status);

	List<String> getResIdInId(List<String> list);

	long updateResBatch(String tenantId,List<Resource> resources);

	long deleteAuthorizationRes(String tenantId,List<String> ids);

	List<String> getAuthorizationResIds(String tenantId, int ttl);

	List<String> getAllTenantId();
	
	Resource queryResById(String id,String tenantId);

	long insertResCountBatch(List<ResourceCount> list);

	int getResCountByTenantId(String tenantId);

	List<MetricSpanTime> getMetricSpanTime();

	List<SimpleResource> queryByTenantIdAndTags(String tenantId,List<Tag> tags);

	long insert(String tenantId,List<Resource> resources);
	/**
	 * 获取所有tag（包括host tag）
	 * @param tenantId
	 * @return
	 */
	List<String> queryAllResTags(String tenantId);
	
	/**
	 * 根据租户id获取该租户的资源id列表
	 * @param tenantId
	 * @return
	 */
	List<String> getAllResId(String tenantId);

	/**
	 * 根据指标名模糊查询匹配的资源标签
	 * @param metrics
	 * @return
     */
	List<Map<String, String>> getResTagsLikeMetrics(String tenantId, List<String> metrics);

	List<Resource> getTenantResByTags(String tenantId, List<String> resTags, String sortField, String sortOrder);

	List<Resource> getResByIpaddrs(String tenantId, List<String> ipaddrs);

	int updateUserTagsBatch(List<Resource> list);
}
