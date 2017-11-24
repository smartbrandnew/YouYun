package uyun.bat.datastore.logic;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.QueryBuilder;
import uyun.bat.datastore.api.serviceapi.entity.ResourceServiceQuery;
import uyun.bat.datastore.api.serviceapi.entity.ServiceApiResMetrics;
import uyun.bat.datastore.entity.ResourceMetrtics;

public interface MetricLogic {

	List<PerfMetric> querySeries(QueryBuilder queryBuilder, int interval, List<String> excludes);

	long insertPerf(List<PerfMetric> metrics);

	boolean delete(QueryBuilder queryBuilder);

	List<PerfMetric> queryTopN(QueryBuilder builder, int n);

	List<PerfMetric> queryPerf(QueryBuilder builder);

	List<Tag> getTags(String tenantId, String metricName);

	Set<String> getGroupTagName(String tenantId, String metricName);

	boolean deleteMetricNamesByResId(String resId);

	long deleteMetricNamesBatch(List<String> resIds);

	boolean deleteTrashData(String metricName, String tenantId, Map<String, String> tags);

	ResourceMetrtics getResMetricNamesByResId(String resourceId);

	/**
	 * 获取指标的最近一个值
	 * 
	 * @param queryBuilder
	 * @return
	 */
	PerfMetric queryLastPerf(QueryBuilder queryBuilder);

	/**
	 * 根据指标tag获取tag
	 * 
	 * @param tenantId
	 * @param metricName
	 * @param tags
	 * @return
	 */
	List<Tag> getTagsByTag(String tenantId, String metricName, List<Tag> tags);

	/**
	 * 根据tenantId获取metricName
	 * 
	 * @param tenantId
	 * @return
	 */

	List<String> getMetricNamesByTenantId(String tenantId);

	// 获取插入吞吐量计数器值
	long getMetricInsertAtomic();

	// 获取查询吞吐量计数器值
	long getMetricQueryAtomic();

	// 获取插入失败计数器值
	long getMetricInsertFailedAtomic();

	// 获取查询失败计数器值
	long getMetricQueryFaileAtomic();

	/**
	 * 指标监测器查询接口
	 * 
	 * @param queryBuilder
	 * @return
	 */
	List<PerfMetric> queryPerfForMonitor(QueryBuilder queryBuilder);

	/**
	 * 仪表盘时序查询group by
	 * 
	 * @param queryBuilder
	 * @param interval
	 * @return
	 */
	Map<String, PerfMetric> querySeriesGroupBy(QueryBuilder queryBuilder, int interval);
	
	/**
	 *
	 * @param query
	 * @return key:应用名称, value:指标列表
	 */
	List<ServiceApiResMetrics> getMetricNames(ResourceServiceQuery query);
	
	PerfMetric queryCurrentPerfMetric(QueryBuilder builder);

}
