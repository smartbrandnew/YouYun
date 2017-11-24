package uyun.bat.datastore.api.service;

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

public interface ResourceService {
	/**
	 * 插入资源(异步)
	 * 
	 * @param resource
	 * @return
	 */
	boolean insertAsync(Resource resource);

	/**
	 * 更新资源(异步)
	 * 
	 * @param resource
	 * @return
	 */
	boolean updateAsync(Resource resource);

	/**
	 * 根据资源ID和租户ID删除资源
	 * 
	 * @param resourceId
	 * @param tenantId
	 * @return
	 */
	boolean delete(String tenantId, String resourceId);

	/**
	 * 批量插入资源
	 * 
	 * @param resources
	 * @param tenantId
	 * @return
	 */
	long insertAsync(List<Resource> resources, String tenantId);

	/**
	 * 根据tenantId、是否包含网络设备查询资源
	 * 
	 * @param tenantId
	 * @param isContainNetwork
	 * @return
	 */
	List<Resource> queryAllRes(String tenantId, boolean isContainNetwork);

	/**
	 * 根据tenantId查询资源Tag
	 * 
	 * @param tenantId
	 * @return
	 */

	List<Tag> queryResTags(String tenantId);

	/**
	 * 根据tenant、filter、group by获取资源分页分组信息,onlineStatus可为null
	 * 
	 * @param tenantId
	 * @param filter 格式("10.1.10.7,tom,host:mypc,dd:mysql,sss",关键字用逗号隔开
	 * @param groupBy
	 * @param pageNo
	 * @param size
	 * @param onlineStatus
	 * @return
	 */
	PageResourceGroup queryByFilterAndGroupByTag(String tenantId, String filter, String groupBy, int pageNo, int size,
			OnlineStatus onlineStatus);

	/**
	 * 根据tenantId获取资源tagName
	 * 
	 * @param tenantId
	 * @return
	 */
	List<String> queryResTagNames(String tenantId);

	/**
	 * 根据资源ID获取资源
	 * 
	 * @param id
	 * @param tenantId
	 * @return
	 */
	Resource queryResById(String id,String tenantId);

	/**
	 * 根据agentId和租户Id获取资源
	 * 
	 * @param id
	 * @return
	 */
	Resource queryResByAgentId(String agentId, String tenantId);

	/**
	 * 根据关键字(tagk、tagv、hostname、ipaddr、tag)获取资源，onlineStatus可为null
	 * 
	 * @param tenantId
	 * @param key格式("10.1.10.7,tom,host:mypc,dd:mysql,sss",关键字用逗号隔开
	 * @param onlineStatus
	 * @return
	 */
	PageResource queryByKey(String tenantId, String key, int pageNo, int size, OnlineStatus onlineStatus);

	/**
	 * 根据filter查询resource并根据字段进行排序、分页，onlineStatus可为null
	 * 
	 * @param tenantId
	 * @param filter
	 * @param orderBy
	 * @param pageNo
	 * @param size
	 * @param onlineStatus
	 * @return
	 */
	PageResource queryByKeyAndSortBy(String tenantId, String filter, ResourceOrderBy orderBy, int pageNo, int size,
			OnlineStatus onlineStatus);

	/**
	 * 获取当前页的资源以及总条数，onlineStatus可为null
	 * 
	 * @param tenantId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	PageResource queryAllRes(String tenantId, int pageNo, int pageSize, OnlineStatus onlineStatus);

	/**
	 * 获取资源状态事件所需
	 * 
	 * @param tenantId
	 * @param onlineStatus
	 * @param lastCollectTime
	 * @return
	 */
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
	 * 查询某段时间内各租户创建资源的数量
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
	 * 根据状态统计各租户资源数量
	 * @param status
	 * @return
	 */

	List<ResourceCount> getResCountByOnlineStatus(OnlineStatus status);


	/********************************* 以上接口for门户 *******************************************/


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

	/**
	 * 根据租户ID和tags获取资源信息
	 * 主机监测器使用
	 * @param tenantId
	 * @param tags
	 * @return
	 */
	List<SimpleResource> queryByTenantIdAndTags(String tenantId, List<Tag> tags);

	/**
	 * 根据租户ID获取所有tags
	 * 包含host标签
	 * @param tenantId
	 * @return
	 */
	List<String> queryAllResTags(String tenantId);

	/**
	 * 实时保存资源到redis/mysql(同步)
	 * @param resource
	 * @return
	 */
	boolean saveResourceSync(Resource resource);

	/**
	 * 实时保存资源到redis/mysql(同步)
	 * 只保存monitor端资源，没有同步store
	 * @param resource
	 * @return
	 */
	boolean saveResourceSyncOnly(Resource resource);

	/**
	 * 根据指标名模糊查询匹配的资源标签
	 * @param metrics
	 * @return
	 */
	List<Map<String, String>> queryResTagsByMetrics(String tenantId, List<String> metrics);

	/**
	 * 资源范围查询资源
	 * @return
	 */
	List<Resource> queryTenantResByTags(String tenantId, List<String> resTags, String sortField, String sortOrder);

	List<Resource> queryResourcesByIpaddrs(String tenantId, List<String> ipaddrs);

	/**
	 * 根据ip批量更新资源userTag
	 * @return
     */
	void updateUserTagsBatch(List<Resource> list);

	/**
	 * 临时提供
	 * monitor resourceId ==> store unitId
	 * 转换成统一资源库资源id
	 *  tenantId
	 *  resId
	 * @return unit_id
	 */
	String resIdTransform(String tenantId, String resId);
}
