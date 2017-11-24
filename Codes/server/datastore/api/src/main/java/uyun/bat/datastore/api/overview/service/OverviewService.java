package uyun.bat.datastore.api.overview.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uyun.bat.datastore.api.overview.entity.ResourceMonitorRecord;
import uyun.bat.datastore.api.overview.entity.TagResourceData;

public interface OverviewService {
	/**
	 * 获取该租户的总览标签Key列表
	 * 
	 * @param tenantId
	 */
	List<String> getOverviewTagKeyList(String tenantId);

	/**
	 * 查询总览统计树
	 * 
	 * @param tenantId
	 * @return
	 */
	List<TagResourceData> getOverviewData(String tenantId);

	/**
	 * 总览堆图<br>
	 * 选择全部或者选择标签key，查询相关的标签分组资源数据统计
	 * 
	 * @param tenantId
	 * @param key
	 * @param value
	 * @return
	 */
	List<TagResourceData> getTagResourceDataList(String tenantId, String key);

	/**
	 * 选择总览标签，查询相关的标签资源数据统计
	 * 
	 * @param tenantId
	 * @param key
	 * @param value
	 * @return
	 */
	TagResourceData getTagResourceData(String tenantId, String key, String value);

	/**
	 * 返回租户的资源告警信息
	 *
	 * @param tenantId
	 * @return
	 */
	Map<String, ResourceMonitorRecord> queryResourceMonitorRecord(String tenantId);

	/**
	 * 返回租户监测器错误状态的资源Id列表
	 *
	 * @param tenantId
	 * @return
	 */
	Set<String> queryResIdByErrorRecord(String tenantId);

}
