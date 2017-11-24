package uyun.bat.datastore.service.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.api.entity.*;
import uyun.bat.datastore.api.service.MetricMetaDataService;
import uyun.bat.datastore.api.service.MetricService;
import uyun.bat.datastore.api.serviceapi.entity.ResourceServiceQuery;
import uyun.bat.datastore.api.serviceapi.entity.ServiceApiResMetrics;
import uyun.bat.datastore.api.util.DateUtil;
import uyun.bat.datastore.api.util.CollatorComparator;
import uyun.bat.datastore.entity.ResourceMetrtics;
import uyun.bat.datastore.logic.MetricLogic;
import uyun.bat.datastore.logic.redis.MetricRedisService;

public class MetricServiceImpl implements MetricService {
	@Autowired
	private MetricLogic metricLogic;
	@Autowired
	private MetricRedisService metricRedisService;
	@Autowired
	private MetricMetaDataService metricMetaDataService;

	public List<PerfMetric> querySeries(QueryBuilder queryBuilder, int interval) {
		return metricLogic.querySeries(queryBuilder, interval, null);
	}

	public boolean delete(QueryBuilder queryBuilder) {
		return metricLogic.delete(queryBuilder);
	}

	public List<PerfMetric> queryTopN(QueryBuilder builder, int n) {
		return metricLogic.queryTopN(builder, n);
	}

	public PerfMetric queryPerf(QueryBuilder builder) {
		List<PerfMetric> list = metricLogic.queryPerf(builder);
		if (list.size() > 0)
			return list.get(list.size() - 1);
		else
			return null;
	}

	public List<Tag> getTags(String tenantId, String metricName) {
		return metricLogic.getTags(tenantId, metricName);
	}

	public Set<String> getGroupTagName(String tenantId, String metricName) {
		return metricLogic.getGroupTagName(tenantId, metricName);
	}

	@Override
	public boolean deleteTrashData(String metricName, String tenantId, Map<String, String> tags) {
		return metricLogic.deleteTrashData(metricName, tenantId, tags);
	}

	@Override
	public List<String> getMetricNamesByResId(String resourceId) {
		List<String> list = metricRedisService.getMetricNamesByResId(resourceId);
		if (list == null || list.size() <= 0)
		{
			ResourceMetrtics resMetrics = metricLogic.getResMetricNamesByResId(resourceId);
			if (resMetrics != null) {
				list = resMetrics.getMetricNames();
				metricRedisService.addMetricNames(resMetrics);
			}
		}
		return list;
	}

	@Override
	public PerfMetric queryLastPerf(QueryBuilder queryBuilder) {
		return metricLogic.queryLastPerf(queryBuilder);
	}

	@Override
	public List<Tag> getTagsByTag(String tenantId, String metricName, List<Tag> tags) {
		return metricLogic.getTagsByTag(tenantId, metricName, tags);
	}

	@Override
	public List<String> getMetricNamesByTenantId(String tenantId) {
//		List<String> list = metricRedisService.getMetricNamesByTenantId(tenantId);
//		if (list.size() <= 0) {
//			list = metricLogic.getMetricNamesByTenantId(tenantId);
//			metricRedisService.addMetricNamesTenantId(tenantId, list.toArray(new String[] {}));
//		}
		//改成直接从mysql拿数据
		List<String> list = metricLogic.getMetricNamesByTenantId(tenantId);
		list.sort(new CollatorComparator());
		return list;
	}

	@Override
	public List<PerfMetric> queryPerfForMonitor(QueryBuilder queryBuilder) {
		return metricLogic.queryPerfForMonitor(queryBuilder);
	}

	@Override
	public List<PerfMetric> queryPerfForCircle(QueryBuilder queryBuilder) {
		return metricLogic.queryPerf(queryBuilder);
	}

	@Override
	public Map<String, PerfMetric> querySeriesGroupBy(QueryBuilder queryBuilder, int interval) {
		return metricLogic.querySeriesGroupBy(queryBuilder, interval);
	}

	@Override
	public List<ServiceApiResMetrics> getMetricNames(ResourceServiceQuery query) {
		return metricLogic.getMetricNames(query);
	}

	@Override
	public PerfMetric queryCurrentPerfMetric(QueryBuilder builder) {
		return metricLogic.queryCurrentPerfMetric(builder);
	}

	@Override
	public List<ResourceMetrics> queryPerfForEachResource(List<Resource> resources, List<String> metricsArr,
														  String tenantId, String sortField, String sortOrder,
														  String type, Long start, Long end) {
		Long startTime = 0L;
		Long endTime = 0L;
		Integer interval = 0;
		if (start == null && end == null) {
			switch (type) {
				case "daily":
					//获取昨天时间
					Date lastDate = DateUtil.getAnyDate(-1);
					startTime = DateUtil.getMorning(lastDate).getTime();
					endTime = DateUtil.getNight(lastDate).getTime();
					//昨日数据的间隔为10分钟
					interval = 60 * 10;
					break;
				case "weekly":
					//获取上周时间
					Date lastWeekDate = DateUtil.getAnyDate(-7);
					startTime = DateUtil.getFirstDayofWeek(lastWeekDate).getTime();
					endTime = DateUtil.getLastDayofWeek(lastWeekDate).getTime();
					//上周数据的间隔为一小时
					interval = 60 * 60;
					break;
				case "monthly":
					Calendar c = Calendar.getInstance();
					//获取上月时间
					int lastMonth = c.get(Calendar.MONTH) - 1;
					int year = c.get(Calendar.YEAR);
					startTime = DateUtil.getFirstDayofMonth(lastMonth, year).getTime();
					endTime = DateUtil.getLastDayofMonth(lastMonth, year).getTime();
					//上月数据的间隔为4小时
					interval = 60 * 60 * 4;
					break;
				default:
					break;
			}
		} else {
			startTime = start;
			endTime = end;
			if (endTime == 0) {
				endTime = new Date().getTime();
			}
			interval = 60 * 10;
		}
		List<ResourceMetrics> results = new ArrayList<>();
		if (metricsArr != null && metricsArr.size() > 0) {
			Map<String, Map<String, double[][]>> metricDataMap = new HashMap<>();
			//循环获取各个指标数据
			for (String metricName : metricsArr) {
				Map<String, double[][]> metricMap =
						generateMetric(metricName, tenantId, startTime, endTime, interval);
				metricDataMap.put(metricName, metricMap);
			}
			for (Resource resource : resources) {
				ResourceMetrics rm = new ResourceMetrics();
				rm.setId(resource.getId());
				rm.setHostname(resource.getHostname());
				rm.setIpaddr(resource.getIpaddr());
				rm.setModified(resource.getModified());
				rm.setSortField(sortField);
				rm.setSortOrder(sortOrder);
				double[][] points;
				List<ResourceMetrics.MetricVal> metrics = new ArrayList<>();
				for (String metricName : metricsArr) {
					Map<String, double[][]> metricMap = metricDataMap.get(metricName);
					points = metricMap.get(resource.getId());
					ResourceMetrics.MetricVal dataPoint = rm.new MetricVal(points, metricName);
					dataPoint.setValAvg(countPointsAvgVal(points));
					metrics.add(dataPoint);
					if (sortField != null && sortField.equals(metricName)) {
						rm.setValAvg(countPointsAvgVal(points));
					}
				}
				rm.setMetrics(metrics);
				results.add(rm);
			}
		}
		List<ResourceMetrics> newResults = new ArrayList<>();
		if (!"hostname".equals(sortField) && !"ipaddr".equals(sortField)) {
			//指标均值排序
			//将均值为null的始终置于队尾
			List<ResourceMetrics> nonValResults = new ArrayList<>();
			if (results.size() > 0) {
				for (int i = 0; i < results.size(); i++) {
					ResourceMetrics result = results.get(i);
					Double avg = result.getValAvg();
					if (avg == null) {
						nonValResults.add(result);
					} else {
						newResults.add(result);
					}
				}
				if (newResults.size() > 0) {
					Collections.sort(newResults);
				}
				newResults.addAll(nonValResults);
			}
		} else {
			newResults = results;
		}
		return setterMetricsUnit(newResults, metricsArr);
	}

	private List<ResourceMetrics> setterMetricsUnit(List<ResourceMetrics> results, List<String> metricsArr) {
		if (metricsArr == null || metricsArr.size() == 0) {
			return results;
		}
		List<MetricMetaData> metaDataList = metricMetaDataService.getMetricsUnitByList(metricsArr);
		if (results != null && results.size() > 0 && metaDataList != null && metaDataList.size() > 0) {
			for (ResourceMetrics result : results) {
				List<ResourceMetrics.MetricVal> metrics = result.getMetrics();
				if (metrics == null || metrics.size() < 1) {
					continue;
				}
				for (ResourceMetrics.MetricVal metric : metrics) {
					String mName = metric.getMetricName();
					if (mName == null || "".equals(mName)) {
						continue;
					}
					for (MetricMetaData metricMetaData : metaDataList) {
						if (mName.equals(metricMetaData.getName())) {
							metric.setUnit(metricMetaData.getUnit());
						}
					}
				}
			}
		}
		return results;
	}

	/**
	 * 获取指定时间以及间隔的指标数据
	 * @param metricName
	 * @param tenantId
	 * @param startTime
	 * @param endTime
	 * @param interval
	 * @return
	 */
	private Map<String, double[][]> generateMetric(String metricName, String tenantId,
												   Long startTime, Long endTime, Integer interval) {
		Map<String, double[][]> map = new HashMap<String, double[][]>();
		QueryBuilder queryBuilder = new QueryBuilder();

		queryBuilder.setStartAbsolute(startTime);
		queryBuilder.setEndAbsolute(endTime);

		QueryMetric metric = queryBuilder.addMetric(metricName).addTenantId(tenantId);
		metric.addGrouper("resourceId");
		// 取平均值
		metric.addAggregatorType(AggregatorType.checkByName("avg"));
		List<PerfMetric> perfMetricList = querySeries(queryBuilder, interval);
		if (perfMetricList != null && perfMetricList.size() > 0) {
			for (PerfMetric p : perfMetricList) {
				// 按理说只能返回一组数据，多组数据是bug
				List<DataPoint> dataPoints = p.getDataPoints();
				if (dataPoints != null && dataPoints.size() > 0) {
					double[][] point = new double[dataPoints.size()][2];
					map.put(p.getResourceId(), point);
					for (int j = 0; j < dataPoints.size(); j++) {
						point[j][0] = dataPoints.get(j).getTimestamp();
						DecimalFormat dcmFmt = new DecimalFormat("0.0");
						String value = dcmFmt.format(Double.parseDouble(dataPoints.get(j).getValue().toString()));
						point[j][1] = Double.parseDouble(value);
					}
				}
			}
		}
		return map;
	}

	/**
	 * 计算一段时序数据的均值
	 * @param points
	 * @return
	 */
	private Double countPointsAvgVal(double[][] points) {
		if (points != null && points.length > 0) {
			double sum = 0.0;
			for (int i = 0; i < points.length; i++) {
				sum += points[i][1];
			}
			Double avg = sum/points.length;
			BigDecimal bg = new BigDecimal(avg);
			return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		return null;
	}
}
