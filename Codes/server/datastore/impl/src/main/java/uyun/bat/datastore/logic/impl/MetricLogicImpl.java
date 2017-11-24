package uyun.bat.datastore.logic.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.apache.commons.net.telnet.TelnetClient;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.kairosdb.client.DataPointTypeRegistry;
import org.kairosdb.client.HttpClient;
import org.kairosdb.client.JsonMapper;
import org.kairosdb.client.builder.QueryMetric;
import org.kairosdb.client.builder.TimeUnit;
import org.kairosdb.client.builder.aggregator.SamplingAggregator;
import org.kairosdb.client.builder.grouper.TagGrouper;
import org.kairosdb.client.response.Queries;
import org.kairosdb.client.response.QueryResponse;
import org.kairosdb.client.response.Response;
import org.kairosdb.client.response.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.api.entity.AggregatorType;
import uyun.bat.datastore.api.entity.DataPoint;
import uyun.bat.datastore.api.entity.MetricMetaData;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.QueryBuilder;
import uyun.bat.datastore.api.entity.RelativeTime;
import uyun.bat.datastore.api.mq.MetricInfo;
import uyun.bat.datastore.api.serviceapi.entity.MetricBean;
import uyun.bat.datastore.api.serviceapi.entity.ResourceServiceQuery;
import uyun.bat.datastore.api.serviceapi.entity.ServiceApiResMetrics;
import uyun.bat.datastore.balance.KairosdbLoadBalancer;
import uyun.bat.datastore.dao.MetricMetaDataDao;
import uyun.bat.datastore.dao.MetricResDao;
import uyun.bat.datastore.dao.ResourceDao;
import uyun.bat.datastore.entity.CalculateTime;
import uyun.bat.datastore.entity.MetricTranslate;
import uyun.bat.datastore.entity.ResourceMetrtics;
import uyun.bat.datastore.entity.TagMetric;
import uyun.bat.datastore.entity.TagQuery;
import uyun.bat.datastore.logic.DistributedUtil;
import uyun.bat.datastore.logic.LogicManager;
import uyun.bat.datastore.logic.MetricLogic;
import uyun.bat.datastore.mq.MQManager;
import uyun.bat.datastore.util.HttpUtil;
import uyun.bat.datastore.util.MetricTrashCleaner;
import uyun.bat.datastore.util.StringUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.netflix.loadbalancer.Server;
public class MetricLogicImpl implements MetricLogic {
	@Resource
	private MetricResDao metricResDao;
	@Resource
	private MetricMetaDataDao metricMetaDataDao;
	@Resource
	private ResourceDao resourceDao;
	@Autowired
	private MetricTrashCleaner metricCleaner;
	@Autowired
	private KairosdbLoadBalancer kairosdbLoadBalancer;
	private static final Logger logger = LoggerFactory.getLogger(MetricLogicImpl.class);
	private String ipaddr;
	private int telnetPort;
	private int httpPort;
	private ConcurrentHashMap<String, Integer> precisionMap = new ConcurrentHashMap<String, Integer>();
	private static int corePoolSize = 3;
	// 设置1小时同步一次
	private static long period = 120;
	// 插入吞吐量计数器
	private static AtomicLong atomicInertLong = new AtomicLong();
	// 查询吞吐量计数器
	private static AtomicLong atomicQueryLong = new AtomicLong();
	// 插入失败次数计数器
	private static AtomicLong atomicFailedInsert = new AtomicLong();
	// 查询失败次数计数器
	private static AtomicLong atomicFailedQuery = new AtomicLong();
	// 设置一个默认值
	private String timeAlignmentType = "StartTimeAlignment";

	/**
	 * 时间间隔
	 */
	@SuppressWarnings("unused")
	private void init() {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize);
		logger.info("batch update metric_resource thread start......");
		service.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (!DistributedUtil.isLeader())
					return;
				if (logger.isDebugEnabled())
					logger.debug("batch update metric_resource task start......");
				try {
					List<ResourceMetrtics> insert = new ArrayList<ResourceMetrtics>();
					List<ResourceMetrtics> update = new ArrayList<ResourceMetrtics>();
					Map<String, ResourceMetrtics> map = new HashMap<String, ResourceMetrtics>();
					List<String> list = LogicManager.getInstance().getMetricRedisService().getAsyncMetricNames();
					List<String> list1 = new ArrayList<String>(list);
					if (list.size() > 0) {
						List<String> updateIds = metricResDao.getResIdInId(list);
						list.removeAll(updateIds);
						for (String id : updateIds) {
							ResourceMetrtics resMetric = LogicManager.getInstance().getMetricRedisService()
									.getResourceMetric(id);
							if (resMetric != null)
								update.add(resMetric);
						}
						for (String id : list) {
							ResourceMetrtics resMetric = LogicManager.getInstance().getMetricRedisService()
									.getResourceMetric(id);
							if (resMetric != null)
								map.put(id, resMetric);
						}
						insert.addAll(map.values());

						// 每批50、分批插入，避免受到mysql包大小限制
						if (update.size() > 0) {
							int index = update.size() / 50;
							if (index == 0)
								metricResDao.batchUpdate(update);
							else {
								for (int i = 0; i < index; i++) {
									List<ResourceMetrtics> updateList = update.subList(50 * i, 50 * (i + 1));
									metricResDao.batchUpdate(updateList);
								}
								metricResDao.batchUpdate(update.subList(50 * index, update.size()));
							}
						}
						if (insert.size() > 0) {
							int index = insert.size() / 50;
							if (index == 0)
								metricResDao.batchInsert(insert);
							else {
								for (int i = 0; i < index; i++) {
									List<ResourceMetrtics> insertList = insert.subList(50 * i, 50 * (i + 1));
									metricResDao.batchInsert(insertList);
								}
								metricResDao.batchInsert(insert.subList(50 * index, insert.size()));
							}
						}
						LogicManager.getInstance().getMetricRedisService()
								.deleteMetricResIds(list1.toArray(new String[] {}));
					}
				} catch (Throwable e) {
					logger.warn("batch update metric_resource exception:{} ", e);
				}
			}
		}, 120, period, java.util.concurrent.TimeUnit.SECONDS);
	}

	public String getIpaddr() {
		return ipaddr;
	}

	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}

	public int getTelnetPort() {
		return telnetPort;
	}

	public void setTelnetPort(int telnetPort) {
		this.telnetPort = telnetPort;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}

	public String getTimeAlignmentType() {
		return timeAlignmentType;
	}

	public void setTimeAlignmentType(String timeAlignmentType) {
		this.timeAlignmentType = timeAlignmentType;
	}

	// 获取插入吞吐量计数器值
	public long getMetricInsertAtomic() {
		return atomicInertLong.longValue();
	}

	// 获取查询吞吐量计数器值
	public long getMetricQueryAtomic() {
		return atomicQueryLong.longValue();
	}

	// 获取插入失败计数器值
	public long getMetricInsertFailedAtomic() {
		return atomicFailedInsert.longValue();
	}

	// 获取查询失败计数器值
	public long getMetricQueryFaileAtomic() {
		return atomicFailedQuery.longValue();
	}

	private byte[] parse(PerfMetric perfMetric) {
		StringBuilder sb = new StringBuilder();
		for (DataPoint point : perfMetric.getDataPoints()) {
			sb.append("put ").append(perfMetric.getName());
			sb.append(" ").append(point.getTimestamp());
			sb.append(" ").append(point.getValue());
			for (String key : perfMetric.getTags().keySet()) {
				List<String> list = perfMetric.getTags().get(key);
				StringBuilder sb1 = new StringBuilder();
				for (String str : list) {
					sb1.append(str).append(",");
				}
				String s = sb1.substring(0, sb1.lastIndexOf(","));
				sb.append(" ").append(key).append("=").append(s);
			}
			sb.append("\n");
		}
		return sb.toString().getBytes();
	}

	public boolean delete(QueryBuilder queryBuilder) {
		try {
			org.kairosdb.client.builder.QueryBuilder builder = changeBuilderType(queryBuilder);
			Response response = kairosdbLoadBalancer.getKairosdbClient().delete(builder);
			if (response.getStatusCode() == 204 && response.getErrors().size() == 0)
				return true;
		} catch (URISyntaxException e) {
			logger.warn("uri illegal error：", e);
		} catch (IOException e) {
			logger.warn("IO error: ", e);
		}
		return false;
	}

	public List<PerfMetric> querySeries(QueryBuilder queryBuilder, int interval, List<String> excludes) {
		HttpClient client = kairosdbLoadBalancer.getTscachedClient();
		SamplingAggregator samplingAggregator = new SamplingAggregator(queryBuilder.getMetric().getAggregatorType().name(), interval,
				TimeUnit.SECONDS);
		if ("StartTimeAlignment".equalsIgnoreCase(timeAlignmentType)) {
			samplingAggregator = samplingAggregator.withStartTimeAlignment();
		} else if ("SamplingAlignment".equalsIgnoreCase(timeAlignmentType)) {
			samplingAggregator = samplingAggregator.withSamplingAlignment();
		}
		return generateQueryPerf(queryBuilder, client, 0, excludes, null, 0, null, samplingAggregator);
	}

	@Override
	public Map<String, PerfMetric> querySeriesGroupBy(QueryBuilder queryBuilder, int interval) {
		List<String> groupByList = queryBuilder.getMetric().getGroupers();
		if (groupByList == null || groupByList.size() <= 0)
			throw new RuntimeException("query series for group error: group can not be null");
		groupByList = new ArrayList<String>(groupByList);
		Map<String, PerfMetric> map = new HashMap<String, PerfMetric>();
		List<String> excludes = queryBuilder.getMetric().getExcludes();

		List<PerfMetric> list = querySeries(queryBuilder, interval, excludes);
		if (list.size() > 0) {
			if (groupByList.size() <= 0) {
				map.put("*", list.get(list.size() - 1));
			} else {
				for (PerfMetric perfMetric : list) {
					List<String> keyList = new ArrayList<String>();
					for (String groupBy : groupByList) {
						for (Entry<String, List<String>> entry : perfMetric.getTags().entrySet()) {
							if (groupBy != null && groupBy.equals(entry.getKey())) {
								List<String> tagvList = entry.getValue();
								StringBuilder sb = new StringBuilder();
								for (String str : tagvList) {
									sb.append(str);
									sb.append(",");
								}
								String tagv = sb.substring(0, sb.lastIndexOf(","));
								String tag = entry.getKey() + ":" + tagv;
								keyList.add(tag);
							}
						}
					}
					if (keyList.size() > 0)
						map.put(keyList.toString(), perfMetric);
					else
						map.put("other", perfMetric);
				}
			}
		}
		return map;
	}

	/**
	 * 添加排除字段
	 *
	 * @param builder
	 * @param client
	 * @param excludes
	 * @return
	 */
	private List<PerfMetric> query(org.kairosdb.client.builder.QueryBuilder builder, HttpClient client,
			List<String> excludes) {
		Map<String, List<String>> excludeTags = null;
		if (null != excludes && !excludes.isEmpty() && excludes.get(0) != "") {
			excludeTags = new HashMap<>();
			for (String ex : excludes) {
				int index = ex.indexOf(":");
				if (index != -1 && index != (ex.length() - 1)) {
					String tagk = ex.substring(0, index);
					String tagv = ex.substring(index + 1);
					tagv = MetricTranslate.translateStr(tagv);
					List<String> tempList = new ArrayList<>();
					if(excludeTags.containsKey(tagk)){
						tempList = excludeTags.get(tagk);
						tempList.add(tagv);
						excludeTags.put(tagk,tempList);
					}else{
						tempList.add(tagv);
						excludeTags.put(tagk, tempList);
					}
				} else {
					MetricTranslate.translateStr(ex);
					excludeTags.put(ex, null);
				}
			}
		}
		List<PerfMetric> metrics = new ArrayList<PerfMetric>();
		try {
			QueryResponse response = client.query(builder);
			if (response.getStatusCode() == 200) {
				atomicQueryLong.incrementAndGet();
				List<Queries> list = response.getQueries();
				for (Queries queries : list) {
					List<Results> resultsList = queries.getResults();
					for (Results results : resultsList) {
						// 是否过滤该组数据
						boolean exclude = false;
						Map<String, List<String>> map = results.getTags();
						if (excludeTags != null && map != null && !map.isEmpty()) {
							for (Map.Entry<String, List<String>> excludeTag : excludeTags.entrySet()) {
								if(map.containsKey(excludeTag.getKey())){
									List<String> val = map.get(excludeTag.getKey());
									boolean isEmpty = (val == null ||val.isEmpty());
									if(isEmpty){
										if(excludeTag.getValue() == null){
											exclude = true;
											break;
										}
									}else{
										if(excludeTag.getValue() != null && excludeTag.getValue().containsAll(val)){
											exclude = true;
											break;
										}
									}
								}
							}
						}
						if (exclude)
							continue;
						String name = results.getName();
						List<org.kairosdb.client.builder.DataPoint> datas = results.getDataPoints();
						List<DataPoint> dataPoints = new ArrayList<DataPoint>();
						for (org.kairosdb.client.builder.DataPoint point : datas) {
							dataPoints.add(new DataPoint(point.getTimestamp(), point.getValue()));
						}
						// 判断数据点size是否大于0
						if (dataPoints.size() > 0) {
							PerfMetric metric = new PerfMetric(name, dataPoints, map);
							int precision = getPrecision(name);
							metric.changePrecision(precision);
							MetricTranslate.deTranslate(metric);
							metrics.add(metric);
						}
					}
				}
			}
		} catch (URISyntaxException e) {
			atomicFailedQuery.incrementAndGet();
			logger.warn("uri illegal error：", e);
		} catch (IOException e) {
			atomicFailedQuery.incrementAndGet();
			logger.warn("IO error: ", e);
		}
		return metrics;
	}

	// jjw建议默认精度为1，后续可以考虑直接在表字段accuary设置默认值
	private int getPrecision(String metricName) {
		Integer precision = precisionMap.get(metricName);
		if (precision != null) {
			return precision;
		}
		MetricMetaData data = metricMetaDataDao.queryByName(metricName);
		if (data != null) {
			precision = data.getPrecision();
			precision = precision == 0 ? 1 : precision;
			precisionMap.put(data.getName(), precision);
			return precision;
		}
		return 1;
	}

	public List<PerfMetric> queryTopN(QueryBuilder builder, int n) {
		HttpClient client = kairosdbLoadBalancer.getKairosdbClient();
		List<String> excludes = builder.getMetric().getExcludes();
		// 由于kairosdb返回一个数据点时间太靠前，使用tscached缓存会有BUG，故汇聚5个点
		List<PerfMetric> list = generateQueryPerf(builder, client, 5, excludes, null, 0, null, null);
		TreeMap<String, PerfMetric> map = new TreeMap<String, PerfMetric>();
		DecimalFormat df = new DecimalFormat("00000000000000000000000.0000");
		for (PerfMetric metric : list) {
			if (metric.getDataPoints() != null && metric.getDataPoints().size() > 0) {
				DataPoint dataPoint = metric.getDataPoints().get(metric.getDataPoints().size() - 1);
				double val = (double) dataPoint.getValue();
				if (val <= 0)
					val = -(Double.MAX_VALUE + val);
				List<DataPoint> dataPoints = new ArrayList<DataPoint>();
				dataPoints.add(dataPoint);
				metric.setDataPoints(dataPoints);
				map.put(df.format(val) + metric, metric);
			}
		}
		List<PerfMetric> metrics = new ArrayList<PerfMetric>();
		uyun.bat.datastore.api.entity.QueryMetric.Order order = builder.getMetric().getOrder();
		int size = map.size();
		if (order == null || uyun.bat.datastore.api.entity.QueryMetric.Order.DESCENDING.equals(order)) {
			for (int i = 0; i < n && i < size; i++) {
				PerfMetric metric = map.pollLastEntry().getValue();
				metrics.add(metric);
			}
		} else if (uyun.bat.datastore.api.entity.QueryMetric.Order.ASCENDING.equals(order)) {
			for (int i = 0; i < n && i < size; i++) {
				PerfMetric metric = map.pollFirstEntry().getValue();
				int precision = getPrecision(metric.getName());
				metric.changePrecision(precision);
				MetricTranslate.deTranslate(metric);
				metrics.add(metric);
			}
		}
		return metrics;
	}

	private org.kairosdb.client.builder.QueryBuilder changeBuilderType(QueryBuilder queryBuilder) {
		org.kairosdb.client.builder.QueryBuilder builder = org.kairosdb.client.builder.QueryBuilder.getInstance();
		Long absoluteStart = queryBuilder.getStartAbsolute();
		uyun.bat.datastore.api.entity.RelativeTime relativeStart = queryBuilder.getStartRelative();
		if (absoluteStart != null) {
			builder.setStart(new Date(absoluteStart));
		} else if (relativeStart != null) {
			builder.setStart(relativeStart.getValue(), TimeUnit.valueOf(relativeStart.getUnit()));
		} else {
			throw new IllegalArgumentException("relativeStart time must be set");
		}
		Long absoluteEnd = queryBuilder.getEndAbsolute();
		uyun.bat.datastore.api.entity.RelativeTime relativeEnd = queryBuilder.getEndRelative();
		if (absoluteEnd != null) {
			builder.setEnd(new Date(absoluteEnd));
		} else if (relativeEnd != null) {
			builder.setEnd(relativeEnd.getValue(), TimeUnit.valueOf(relativeEnd.getUnit()));
		} else {
			builder.setEnd(new Date());
		}
		uyun.bat.datastore.api.entity.QueryMetric metric = queryBuilder.getMetric();
		if (!metric.checkSyntax())
			throw new IllegalArgumentException("metric query must contain tenantId");
		Map<String, List<String>> map = metric.getTags();

		QueryMetric queryMetric = builder.addMetric(metric.getName()).addMultiValuedTags(map);

		if (metric.getGroupers().size() > 0) {
			queryMetric.addGrouper(new TagGrouper(metric.getGroupers()));
		}
		return builder;
	}

	private CalculateTime calculatePeriod(QueryBuilder builder, int n) {

		Long startTime = builder.getStartAbsolute();
		Long endTime = builder.getEndAbsolute();

		uyun.bat.datastore.api.entity.RelativeTime startRelativeTime = builder.getStartRelative();
		uyun.bat.datastore.api.entity.RelativeTime endRelativeTime = builder.getEndRelative();
		if (startTime == null) {
			if (startRelativeTime != null) {
				startTime = startRelativeTime.getTimeRelativeTo(System.currentTimeMillis());
			} else
				throw new IllegalArgumentException("relativeStart time must be set：");
		}
		if (endTime == null) {
			if (endRelativeTime != null)
				endTime = endRelativeTime.getTimeRelativeTo(System.currentTimeMillis());
			else
				endTime = System.currentTimeMillis();
		}
		long period = endTime - startTime;
		CalculateTime calculateTime = null;
		if (n <= 0) {
			throw new IllegalArgumentException("Illegal arguments，after time series data merge,datapoint can not less than 0");
		} else {
			int t = 0;
			// 小于1小时用毫秒计算
			if (period < 1000 * 60 * 60 * 1) {
				t = (int) (period / n + 1);
				calculateTime = new CalculateTime(t, TimeUnit.MILLISECONDS);
			} else {
				t = (int) (period / 1000 / 60 / n + 1);
				calculateTime = new CalculateTime(t, TimeUnit.MINUTES);
			}
		}
		return calculateTime;
	}

	public List<Tag> getTags(String tenantId, String metricName) {
		return getTags(tenantId, metricName, new ArrayList<Tag>());
	}

	private List<Tag> getTags(String tenantId, String metricName, List<Tag> condtionTags) {
		List<Tag> tags = new ArrayList<Tag>();
		TagMetric metric = new TagMetric(MetricTranslate.translateStr(metricName),
				MetricTranslate.translateStr(tenantId));
		for (Tag tag : condtionTags) {
			metric.addTag(tag.getKey(), tag.getValue());
		}
		TagQuery tagQuery = new TagQuery(new RelativeTime(365, uyun.bat.datastore.api.entity.TimeUnit.DAYS), metric);
		CloseableHttpClient httpClient = HttpUtil.getClient();
		Server server = kairosdbLoadBalancer.getReadKaios();
		if (server != null) {
			String url = "http://" + server.getHost() + ":" + server.getPort() + "/api/v1/datapoints/query/tags";
			HttpPost post = new HttpPost(url);
			HttpEntity httpEntity = new StringEntity(tagQuery.toJsonString(),
					ContentType.create("application/json", "utf-8"));
			post.setEntity(httpEntity);
			try {
				CloseableHttpResponse httpResponse = httpClient.execute(post);
				int code = httpResponse.getStatusLine().getStatusCode();
				if (code == 204 || code == 200) {
					QueryResponse response = new QueryResponse(new JsonMapper(new DataPointTypeRegistry()), code,
							httpResponse.getEntity().getContent());
					List<Queries> list = response.getQueries();
					for (Queries queries : list) {
						List<Results> resultsList = queries.getResults();
						for (Results results : resultsList) {
							Map<String, List<String>> map = results.getTags();
							for (Entry<String, List<String>> entry : map.entrySet()) {
								if (!"tenantId".equals(entry.getKey()) && !"resourceId".equals(entry.getKey())) {
									String key = MetricTranslate.deTranslateStr(entry.getKey());
									for (String value : entry.getValue()) {
										if (condtionTags.isEmpty()) {
											Tag tag = new Tag(key, MetricTranslate.deTranslateStr(value));
											tags.add(tag);
										} else {
											for (Tag contdtionTag : condtionTags) {
												if (!(key.equals(contdtionTag.getKey()) && MetricTranslate
														.deTranslateStr(value).equals(contdtionTag.getValue()))) {
													Tag tag = new Tag(key, MetricTranslate.deTranslateStr(value));
													tags.add(tag);
												}
											}
										}
									}
								}
							}
						}
					}
				}

				httpResponse.close();
			} catch (ClientProtocolException e) {
				logger.warn("http handle exception：", e);
			} catch (IOException e) {
				logger.warn("IO Exception：", e);
			}
		}
		return tags;
	}

	public Set<String> getGroupTagName(String tenantId, String metricName) {
		List<Tag> list = getTags(tenantId, metricName);
		Set<String> tagNames = new HashSet<String>();
		for (Tag tag : list) {
			tagNames.add(tag.getKey());
		}
		return tagNames;
	}

	private MetricInfo generateMetricInfo(PerfMetric metric) {
		List<Tag> tags = new ArrayList<Tag>();
		for (Entry<String, List<String>> entry : metric.getTags().entrySet()) {
			StringBuilder builder = new StringBuilder();
			for (String value : entry.getValue()) {
				builder.append(value);
				builder.append(",");
			}
			tags.add(new Tag(entry.getKey(), builder.substring(0, builder.length() - 1)));
		}
		return new MetricInfo(metric.getName(), metric.getTenantId(), tags);
	}

	public long insertPerf(List<PerfMetric> metrics) {
		long count = 0l;
		if (metrics.size() > 0) {
			List<String> updateNames = new ArrayList<String>();
			Set<String> set = new HashSet<String>();
			List<MetricInfo> metricInfoList = new ArrayList<MetricInfo>();

			TelnetClient client = kairosdbLoadBalancer.getTelnetClient();

			try {
				for (PerfMetric metric : metrics) {
					// 监测器的条件匹配为非转译的字符,故此处先保存一份转译前的
					PerfMetric temp = metric.clonePerfMetric();
					set.add(metric.getName().trim());

					metric.checkSynstax();
					MetricTranslate.translate(metric);
					byte[] buffer = parse(metric);
					if (client.isConnected()) {
						client.getOutputStream().write(buffer);
						client.getOutputStream().flush();
						atomicInertLong.incrementAndGet();
						count = count + 1;
						metricInfoList.add(generateMetricInfo(temp));
					}
				}
			} catch (IOException e) {
				atomicFailedInsert.incrementAndGet();
				logger.warn("insertPerf error[" + client.getLocalAddress() + "] : " + e.getMessage());
				if (logger.isDebugEnabled())
					logger.debug("stack", e);
				kairosdbLoadBalancer.onTelnetClientError(client);
			}
			MQManager.getInstance().getMetricMQService().metricSaved(metricInfoList);

			updateNames.addAll(set);
			if (StringUtils.isNotNullAndBlank(metrics.get(0).getResourceId())) {
				String resId = metrics.get(0).getResourceId();
				List<String> names = LogicManager.getInstance().getMetricRedisService().getMetricNamesByResId(resId);
				// 再从Mysql中查一遍
				if (names == null || names.size() <= 0) {
					ResourceMetrtics resMetric = metricResDao.getMetricNamesByResId(resId);
					if (resMetric != null) {
						names = resMetric.getMetricNames();
					}
				}
				if (names.size() <= 0) {
					LogicManager.getInstance().getMetricRedisService()
							.addMetricNames(updateNames.toArray(new String[] {}), resId, metrics.get(0).getTenantId());
				} else {
					if (!names.containsAll(updateNames)) {
						updateNames.removeAll(names);
						names.addAll(updateNames);
						LogicManager.getInstance().getMetricRedisService()
								.addMetricNames(names.toArray(new String[] {}), resId, metrics.get(0).getTenantId());
					}
				}
			} else {
				LogicManager.getInstance().getMetricRedisService()
						.addTenantMetricNames(updateNames.toArray(new String[] {}), metrics.get(0).getTenantId());
			}
		}
		return count;
	}

	public List<PerfMetric> queryPerf(QueryBuilder builder) {
		HttpClient client = kairosdbLoadBalancer.getTscachedClient();
		return generateQueryPerf(builder, client, 3, null, null, 0, null, null);
	}

	@Override
	public List<PerfMetric> queryPerfForMonitor(QueryBuilder builder) {
		HttpClient client = kairosdbLoadBalancer.getKairosdbClient();
		return generateQueryPerf(builder, client, 1, null, null, 0, null, null);
	}

	@Override
	public PerfMetric queryCurrentPerfMetric(QueryBuilder builder) {
		HttpClient client = kairosdbLoadBalancer.getTscachedClient();
		List<PerfMetric> metrics = generateQueryPerf(builder, client, 0, null, AggregatorType.last, 1, TimeUnit.HOURS, null);
		if (metrics.size() > 0) {
			return metrics.get(metrics.size() - 1);
		}
		return null;
	}

    @Override
    public PerfMetric queryLastPerf(QueryBuilder builder) {
        // 分两段查询，先查24小时内的最后一条记录，为空的话，再查询6个月之内的最后一条记录，都没有就返回Null
        HttpClient client = kairosdbLoadBalancer.getTscachedClient();
        builder.setStart(1, uyun.bat.datastore.api.entity.TimeUnit.DAYS);

		List<PerfMetric> metrics = generateQueryPerf(builder, client, 0, null, AggregatorType.last, 1, TimeUnit.HOURS, null);
		if (metrics.size() > 0) {
            return metrics.get(metrics.size() - 1);
        } // 查6个月的数据
        else {
			metrics = generateQueryPerf(builder, client, 0, null, AggregatorType.last, 6, TimeUnit.WEEKS, null);
			if (metrics.size() > 0)
                return metrics.get(metrics.size() - 1);
            return null;
        }
    }

	private List<PerfMetric> generateQueryPerf(QueryBuilder builder, HttpClient client, int interval, List<String> excludes,
											  AggregatorType agg, int value, TimeUnit unit, SamplingAggregator samplingAggregator) {
		// 由于tscached汇聚一个点数据时间戳为开始时间，很容易在interval之内查询没有数据，故汇聚为3个点，tscached自动根据interval对齐，保证缓存能够使用
		CalculateTime calculateTime = null;
		if (interval != 0)
			calculateTime = calculatePeriod(builder, interval);
		uyun.bat.datastore.api.entity.QueryMetric metric = builder.getMetric();
		builder.addMetric(metric);
		MetricTranslate.translate(metric);
		org.kairosdb.client.builder.QueryBuilder queryBuilder = changeBuilderType(builder);
		if (null == samplingAggregator) {
			String aggregator = null == agg ? builder.getMetric().getAggregatorType().name() : agg.getName();
			TimeUnit timeUnit = null == unit ? calculateTime.getTimeUnit() : unit;
			int period = value == 0 ? calculateTime.getPeriod() : value;
			queryBuilder.getMetrics().get(0).addAggregator(new SamplingAggregator(aggregator, period, timeUnit));
		} else
			queryBuilder.getMetrics().get(0).addAggregator(samplingAggregator);
		return query(queryBuilder, client, excludes);
	}

	@Override
	public boolean deleteTrashData(String metricName, String tenantId, Map<String, String> tags) {
		if (tenantId == null) {
			throw new IllegalArgumentException("In the condition of delete,tenantId must be set");
		}
		if (tags == null || tags.size() < 1) {
			throw new IllegalArgumentException("In the condition of delete,there must be unless one tag");
		}
		SetMultimap<String, String> map = HashMultimap.create();
		for (Entry<String, String> entry : tags.entrySet()) {
			map.put(entry.getKey(), entry.getValue());
		}
		map.put("tenantId", MetricTranslate.translateStr(tenantId));
		return metricCleaner.deleteMetricData(MetricTranslate.translateStr(metricName), map,
				new RelativeTime(5, uyun.bat.datastore.api.entity.TimeUnit.YEARS));
	}

	@Override
	public ResourceMetrtics getResMetricNamesByResId(String resourceId) {
		return metricResDao.getMetricNamesByResId(resourceId);
	}

	@Override
	public List<Tag> getTagsByTag(String tenantId, String metricName, List<Tag> tags) {
		return getTags(tenantId, metricName, tags);
	}

	@Override
	public List<String> getMetricNamesByTenantId(String tenantId) {
		List<ResourceMetrtics> metricNames = metricResDao.getMetricNamesByTenantId(tenantId);
		List<String> list = new ArrayList<String>();
		Set<String> set = new HashSet<String>();
		for (ResourceMetrtics resMetric : metricNames) {
			if (resMetric != null)
				set.addAll(resMetric.getMetricNames());
		}
		list.addAll(set);
		return list;
	}

	@Override
	public boolean deleteMetricNamesByResId(String resId) {
		return metricResDao.delete(resId);
	}

	@Override
	public long deleteMetricNamesBatch(List<String> resIds) {
		if (resIds.size() > 0)
			return metricResDao.batchDelete(resIds);
		return 0l;
	}

	@Override
	public List<ServiceApiResMetrics> getMetricNames(ResourceServiceQuery query) {
		List<ServiceApiResMetrics> results = new ArrayList<ServiceApiResMetrics>();
		List<String> resourceIds = resourceDao.getResIdByResServieQuery(query);
		if (resourceIds.size() <= 0)
			return null;
		String tenantId = query.getTenantId();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tenantId", tenantId);
		map.put("resourceIds", resourceIds);
		List<ResourceMetrtics> list = metricResDao.getMetricNamesInResId(map);
		Map<String, String> metaDataMap = new HashMap<>();
		List<MetricMetaData> metaDatas = metricMetaDataDao.queryAll(tenantId);
		for (MetricMetaData metricMetaData : metaDatas) {
			metaDataMap.put(metricMetaData.getName(), metricMetaData.getcName());
		}
		for (ResourceMetrtics resMetrics : list) {
			ServiceApiResMetrics metrics = new ServiceApiResMetrics();
			metrics.setResource_id(resMetrics.getResourceId());
			Map<String, Set<String>> metricMap = new HashMap<String, Set<String>>();
			for (String name : resMetrics.getMetricNames()) {
				int index = name.indexOf(".");
				if (index != -1) {
					String app = name.substring(0, index);
					Set<String> metricNames = metricMap.get(app);
					if (metricNames == null)
						metricNames = new HashSet<String>();
					metricNames.add(name);
					metricMap.put(app, metricNames);
				}
			}

			Map<String, List<MetricBean>> metricBeanMap = new HashMap<>();
			for (Entry<String, Set<String>> entry : metricMap.entrySet()) {
				List<MetricBean> metricBeanList = metricBeanMap.get(entry.getKey());
				if (metricBeanList == null)
					metricBeanList = new ArrayList<>();
				metricBeanMap.put(entry.getKey(), metricBeanList);
				for (String metricName : entry.getValue()) {
					MetricBean metricBean = new MetricBean();
					metricBean.setMetric(metricName);
					String cn = metaDataMap.get(metricName);
					metricBean.setDesc(cn);
					metricBeanList.add(metricBean);
				}
			}
			metrics.setMetrics(metricBeanMap);
			results.add(metrics);
		}
		return results;
	}

}
