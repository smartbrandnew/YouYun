package uyun.bat.datastore.overview.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import uyun.bat.datastore.api.overview.entity.ResourceMonitorRecord;
import uyun.bat.datastore.api.overview.entity.TagResourceData;

/**
 * 该dao查询耦合了资源表
 */
public interface ResourceMonitorRecordDao {
	/**
	 * 创建资源监测器状态记录<br>
	 * 重复资源id，监测器id时，若时间戳比当前时间新则更新该条记录，否则则不更新数据
	 * 
	 * @param resourceMonitorRecordList
	 * @return
	 */
	int save(List<ResourceMonitorRecord> resourceMonitorRecordList);

	/**
	 * 查询总览统计树
	 */
	List<TagResourceData> getOverviewData(@Param("tenantId") String tenantId, @Param("beginTime") long beginTime);

	/**
	 * 总览堆图<br>
	 * 选择全部或者选择标签key，查询相关的标签分组资源数据统计
	 */
	List<TagResourceData> getTagResourceDataList(@Param("tenantId") String tenantId, @Param("key") String key, @Param("beginTime") long beginTime);

	/**
	 * 选择总览标签，查询相关的标签资源数据统计
	 */
	List<TagResourceData> getTagResourceData(@Param("tenantId") String tenantId, @Param("key") String key,
			@Param("value") String value, @Param("beginTime") long beginTime);

	int delete(@Param("tenantId") String tenantId, @Param("resourceId") String resourceId,
			@Param("monitorId") String monitorId);

	int deleteDeletedMonitorData(@Param("tenantId") String tenantId, @Param("monitorIdList") List<String> monitorIdList);

	List<String> queryTenantResourceIdList(@Param("tenantId") String tenantId);

	List<ResourceMonitorRecord> queryResourceMonitorRecord(@Param("tenantId") String tenantId,
														   @Param("monitorId") String monitorId, @Param("resourceId") String resourceId);


}
