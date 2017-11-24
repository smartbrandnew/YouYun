package uyun.bat.datastore.api.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.QueryBuilder;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceMetrics;
import uyun.bat.datastore.api.serviceapi.entity.ResourceServiceQuery;
import uyun.bat.datastore.api.serviceapi.entity.ServiceApiResMetrics;

public interface MetricService {
	/**
	 * 仪表盘查询请调用此接口(对数据精度要求不严，用于展现的数据请调此接口)
	 * @param queryBuilder
	 * @param interval
	 * @return
	 */
	List<PerfMetric> querySeries(QueryBuilder queryBuilder, int interval);

	/**
	 * 仅适用于仪表盘资源圈查询接口
	 * @param queryBuilder
	 * @return
	 */

	List<PerfMetric> queryPerfForCircle(QueryBuilder queryBuilder);

	/**
	 * 仪表盘时序查询group by
	 * @param queryBuilder
	 * @param interval
	 * @return
	 */
	Map<String, PerfMetric> querySeriesGroupBy(QueryBuilder queryBuilder, int interval);

	/**
	 * 删除指标
	 * @param queryBuilder
	 * @return
	 */

	boolean delete(QueryBuilder queryBuilder);

	/**
	 * 查询性能指标top n数据
	 * @param builder
	 * @param n
	 * @return
	 */

	List<PerfMetric> queryTopN(QueryBuilder builder, int n);

	/**
	 * 查询性能指标单值
	 * @param builder
	 * @return
	 */
	PerfMetric queryPerf(QueryBuilder builder);

	/**
	 * 获取性能指标 tag
	 * @param tenantId
	 * @param metricName
	 * @return
	 */

	List<Tag> getTags(String tenantId, String metricName);

	/**
	 * 获取性能指标tagk
	 * @param tenantId
	 * @param metricName
	 * @return
	 */
	Set<String> getGroupTagName(String tenantId, String metricName);

	/**
	 * 根据tags删除历史数据
	 * @param metricName
	 * @param tenantId
	 * @param tags
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	boolean deleteTrashData(String metricName, String tenantId, Map<String, String> tags);

	/**
	 * 根据resId获取指标名称
	 * @param resourceId
	 * @return
	 */
	List<String> getMetricNamesByResId(String resourceId);

	List<ServiceApiResMetrics> getMetricNames(ResourceServiceQuery query);

	/**
	 * 获取指标的最近一个值
	 * @param queryBuilder
	 * @return
	 */
	PerfMetric queryLastPerf(QueryBuilder queryBuilder);

	/**
	 * 根据指标tag获取tag
	 * @param tenantId
	 * @param metricName
	 * @param tag
	 * @return
	 */
	List<Tag> getTagsByTag(String tenantId, String metricName, List<Tag> tags);

	/**
	 * 根据tenantId获取metricName
	 * @param tenantId
	 * @return
	 */

	List<String> getMetricNamesByTenantId(String tenantId);

	/**
	 * 以下接口for monitor指标监测器
	 */

	List<PerfMetric> queryPerfForMonitor(QueryBuilder queryBuilder);

	/**
	 * 查询最近1小时,指标最近的值
	 * @param builder
	 * @return
	 */
	PerfMetric queryCurrentPerfMetric(QueryBuilder builder);

	/**
	 * 报表数据查询: 每个资源下指标数据
	 * @param resources 资源列表
	 * @param metricsArr 指标列表
	 * @param sortField 根据指定均值排序 或者 hostname, ipaddr
	 * @param sortOrder desc asc
	 * @param type daily 昨日; weekly 上周; monthly 上月
	 * @param start 开始时间戳(可选)
	 * @param end 结束时间戳(可选)
     * @return
     */
	List<ResourceMetrics> queryPerfForEachResource(List<Resource> resources, List<String> metricsArr,
												   String tenantId, String sortField, String sortOrder,
												   String type, Long start, Long end);

}
