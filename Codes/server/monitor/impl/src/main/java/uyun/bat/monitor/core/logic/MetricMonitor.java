package uyun.bat.monitor.core.logic;

import uyun.bat.datastore.api.entity.*;
import uyun.bat.event.api.entity.Event;
import uyun.bat.event.api.entity.EventServerityType;
import uyun.bat.event.api.entity.EventSourceType;
import uyun.bat.event.api.entity.EventTag;
import uyun.bat.monitor.api.common.util.PeriodUtil;
import uyun.bat.monitor.api.common.util.PeriodUtil.Period;
import uyun.bat.monitor.api.common.util.StateUtil;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.core.entity.*;
import uyun.bat.monitor.core.entity.MetricMonitorParam.Threshold;
import uyun.bat.monitor.core.util.MonitorQueryUtil;
import uyun.bat.monitor.core.util.TagUtil;
import uyun.bat.monitor.impl.common.ServiceManager;
import uyun.bat.monitor.impl.util.JsonUtil;
import uyun.whale.common.util.text.Unit;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;

public class MetricMonitor implements Checker {

	private Monitor monitor;

	private MetricMonitorParam metricMonitorParam;

	/**
	 * 临时保存触发的数据<分组tag，相关数据>，防止既出发告警，又出发警告的情况，以及作为后续发事件用
	 */
	private Map<String, MetricDataPoint> metricDataPoints;

	/**
	 * 查询表达式
	 */
	private String query;

	public MetricMonitor(Monitor monitor, MetricMonitorParam metricMonitorParam) {
		super();
		this.monitor = monitor;
		this.metricMonitorParam = metricMonitorParam;
	}

	/**
	 * 组装监测器的查询逻辑表达式
	 */
	private String getQuery() {
		if (query == null) {
			if (monitor.getOptions() != null && monitor.getOptions().getThresholds() != null
					&& monitor.getOptions().getThresholds().size() > 0) {
				StringBuilder sb = new StringBuilder(monitor.getQuery());
				try {
					String thresholds = JsonUtil.encode(monitor.getOptions().getThresholds());
					sb.append(" ");
					sb.append(thresholds);
				} catch (Exception e) {
					// 告警条件非法，按理说不会在这里出现
				}
				query = sb.toString();
			} else {
				query = monitor.getQuery();
			}

		}
		return query;
	}

	public boolean match(MetricData data) {
		return metricMonitorParam.match(data);
	}

	public MonitorState checkIfMonitorStatusRollover() {
		// 状态翻转才发送事件；若状态不变，则不重复发事件
		MonitorState ms = MonitorState.OK;
		for (Entry<String, List<Threshold>> entry : metricMonitorParam.getPeriodThresholdMap().entrySet()) {
			MonitorState temp = checkIfMonitorStatusRollover(entry.getKey(), entry.getValue());
			if (temp.getValue() > ms.getValue())
				ms = temp;
		}
		return ms;
	}

	/**
	 * 20160728版本变更监测器设置，一个指标监测器可以设置多个不同时间窗格的阈值
	 */
	private MonitorState checkIfMonitorStatusRollover(String period, List<Threshold> thresholds) {
		QueryBuilder queryBuilder = new QueryBuilder();

		Period p = PeriodUtil.generatePeriod(period);
		queryBuilder.setStartAbsolute(p.getStart());
		queryBuilder.setEndAbsolute(p.getEnd());
		QueryMetric metric = queryBuilder.addMetric(metricMonitorParam.getMetric()).addTenantId(monitor.getTenantId());
		if (metricMonitorParam.getTags() != null && metricMonitorParam.getTags().size() > 0) {
			for (TagEntry te : metricMonitorParam.getTags()) {
				metric.addTag(te.getKey(), te.getValue());
			}
		}

		if (metricMonitorParam.getGroups() != null && metricMonitorParam.getGroups().size() > 0) {
			for (String group : metricMonitorParam.getGroups())
				metric.addGrouper(group);
		}

		metric.addAggregatorType(AggregatorType.checkByName(metricMonitorParam.getAggregator().getCode()));
		// 取单值
		List<PerfMetric> perfMetrics = ServiceManager.getInstance().getMetricService()
				.queryPerfForMonitor(queryBuilder);

		if (perfMetrics == null || perfMetrics.size() == 0)
			return monitor.getMonitorState();

		if (metricDataPoints == null)
			metricDataPoints = new HashMap<String, MetricDataPoint>();

		// 此次时间窗格的数据，匹配的最高告警等级
		MonitorState ms = MonitorState.OK;

		for (PerfMetric perfMetric : perfMetrics) {
			if (perfMetric.getDataPoints() == null || perfMetric.getDataPoints().size() == 0)
				continue;
			// 应该获取到的只有一个点,取最后一点
			DataPoint dataPoint = perfMetric.getDataPoints().get(perfMetric.getDataPoints().size() - 1);
			// 获取指标单位
			MetricMetaData metaData = ServiceManager.getInstance().getMetricMetaDataService().queryByName(metricMonitorParam.getMetric());
			try {
				// 按理说value 为 Number，都有doubleValue
				double value = dataPoint.doubleValue();
				String key = generateKey(perfMetric);
				// 假定所有的指标都是正常的,先保存一次正常的指标，后续若匹配阈值，则覆盖它
				MonitorState temp = putData(key, MonitorState.OK, perfMetric, dataPoint.getValue());
				for (Threshold threshold : thresholds) {
					Double tempValue = null;
					// 将指标的值转成与阈值相同单位再比较
					if (null != metaData) {
						Unit metricUnit = Unit.SETS[0][0].getUnitByCode(metaData.getUnit());
						Unit thresholdUnit = Unit.SETS[0][0].getUnitByCode(threshold.getUnit());
						if (null != metricUnit && null != thresholdUnit)
							tempValue = metricUnit.to(thresholdUnit, value);
					}
					// thresholds队列按理说已排序，告警等级从高到低匹配
					boolean match = threshold.getComparison().match(null != tempValue ? tempValue : value, threshold.getValue());
					if (match) {
						MonitorState state = MonitorState.checkByCode(threshold.getThreshold());
						temp = putData(key, state, perfMetric, dataPoint.getValue());
						break;
					}
				}
				if (ms == null || temp.getValue() > ms.getValue())
					ms = temp;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return ms;
	}

	/**
	 * 保存临时数据返回当前的告警等级
	 */
	private MonitorState putData(String key, MonitorState monitorState, PerfMetric perfMetric, Object value) {
		MetricDataPoint temp = metricDataPoints.get(key);
		if (temp == null || temp.monitorState.getValue() <= monitorState.getValue()) {
			// 一个监测器只保存该资源最高等级的告警
			metricDataPoints.put(key, new MetricDataPoint(monitorState, perfMetric, value));
			return monitorState;
		}
		return temp.monitorState;
	}

	private String generateKey(PerfMetric perfMetric) {
		if (metricMonitorParam.getGroups() != null && !metricMonitorParam.getGroups().isEmpty()) {
			// 有分组条件则以分组相关tag为key
			StringBuilder sb = new StringBuilder();
			for (String g : metricMonitorParam.getGroups()) {
				sb.append(g);
				sb.append(":");
				List<String> vs = perfMetric.getTags().get(g);
				if (vs != null) {
					Collections.sort(vs);
					for (String v : vs) {
						sb.append(v);
						sb.append(",");
					}
				}
				sb.append(";");
			}
			return sb.toString();
		} else if (metricMonitorParam.getTags() != null && !metricMonitorParam.getTags().isEmpty()) {
			// 没有分组条件,若有监测器tag则以监测器tag为key
			StringBuilder sb = new StringBuilder();
			for (TagEntry te : metricMonitorParam.getTags()) {
				sb.append(te.getKey());
				sb.append(":");
				sb.append(te.getValue());
				sb.append(";");
			}
			return sb.toString();
		} else {
			// 什么也没有则以*为key
			return "*";
		}
	}

	public Monitor getMonitor() {
		return monitor;
	}

	private Event generateEvent(MetricDataPoint data) {
		Event event = new Event();
		EventServerityType eventServerityType = MonitorQueryUtil.getEventServerityType(data.monitorState);
		if (eventServerityType == null)
			return null;
		else
			event.setServerity(eventServerityType.getKey());

		event.setMsgTitle(monitor.getName());
		event.setResId(data.getResourceId());
		event.setOccurTime(new Timestamp(System.currentTimeMillis()));
		event.setSourceType(EventSourceType.MONITOR.getKey());
		event.setMonitorId(monitor.getId());
		event.setMonitorType(monitor.getMonitorType().getCode());

		// 监测器标签
		List<EventTag> monitorTags = new ArrayList<>();
		if (metricMonitorParam.getTags() != null && metricMonitorParam.getTags().size() > 0) {
			for (TagEntry te : metricMonitorParam.getTags()) {
				EventTag et = new EventTag();
				et.setTenantId(monitor.getTenantId());
				et.setTagk(te.getKey());
				et.setTagv(te.getValue() != null ? te.getValue() : "");
				monitorTags.add(et);
			}
		}

		//指标标签（不包含tenantId和resourceId）
		List<EventTag> tags=new ArrayList<>();
		List<TagEntry> temps = data.getTagList();
		if (temps != null && temps.size() > 0) {
			for (TagEntry te : temps) {
				if ("tenantId".equals(te.getKey())||"resourceId".equals(te.getKey())){
					continue;
				}
				EventTag et = new EventTag();
				et.setTenantId(monitor.getTenantId());
				et.setTagk(te.getKey());
				et.setTagv(te.getValue() != null ? te.getValue() : "");
				tags.add(et);
			}
		}
		tags.removeAll(monitorTags);
		tags.addAll(monitorTags);

		event.setEventTags(tags);
		event.setTenantId(monitor.getTenantId());

		return event;
	}

	public void doAfterCheck() {
		if (metricDataPoints == null || metricDataPoints.size() == 0)
			return;

		for (MetricDataPoint data : metricDataPoints.values()) {
			Event event = generateEvent(data);
			if (event == null)
				continue;
			//值不以科学计数显示
			NumberFormat nf = NumberFormat.getInstance();
			nf.setGroupingUsed(false);

			CheckContext context = new CheckContext(event, data.getResourceId(), data.monitorState,
					nf.format(data.value), metricMonitorParam,MonitorType.METRIC);
			for (TagEntry t : data.getTagList()) {
				if (t.getKey().equals("host"))
					context.setHostName(t.getValue());
				if (t.getKey().equals("ip"))
					context.setIp(t.getValue());
				List<String> groups = metricMonitorParam.getGroups();
				if (groups != null) {
					Optional<String> optional = groups.stream()
							.filter(g -> !StateUtil.RESOURCE_ID.equals(g) && t.getKey().equals(g))
							.findFirst();
					if (optional.isPresent()) {
						context.setInstance(t.toString());
					}
				}
			}
			boolean trigger = CheckController.getInstance().trigger(monitor, context, generateSymbol(data));
			if (trigger && monitor.getNotify() && monitor.getNotifyUserIdList() != null
					&& monitor.getNotifyUserIdList().size() > 0) {
				CheckController.getInstance().notify(context, monitor.getNotifyUserIdList());
			}
		}
	}

	/**
	 * 创建事件标识
	 * 
	 * @param
	 * @return
	 */
	private Symbol generateSymbol(MetricDataPoint data) {
		Symbol symbol = new Symbol();
		symbol.setMonitorType(monitor.getMonitorType());
		symbol.setTenantId(monitor.getTenantId());
		symbol.setMonitorId(monitor.getId());
		symbol.setMonitorState(data.monitorState);
		symbol.setQuery(getQuery());

		List<TagEntry> tags = null;
		List<String> groups = metricMonitorParam.getGroups();
		if (groups != null && groups.size() > 0) {
			tags = new ArrayList<TagEntry>();
			// 默认分组,则需要添加上分组时，相应指标中对应的tag
			// 按理说添加上分组条件就已足够事件台那边汇聚事件
			// 然后checkpoint的状态，在监测器更新后，应该能删掉相关数据
			List<TagEntry> temps = data.getTagList();

			for (String group : groups) {
				for (TagEntry te : temps) {
					if (group.equals(te.getKey())) {
						tags.add(te);
					}
					if (StateUtil.RESOURCE_ID.equals(te.getKey())) {
						symbol.setResourceId(te.getValue());
					}
				}
			}
			TagUtil.generateTags(tags);
		}
		symbol.setTags(tags);
		return symbol;
	}

	public MetricMonitorParam getMetricMonitorParam() {
		return metricMonitorParam;
	}

	public void setMetricMonitorParam(MetricMonitorParam metricMonitorParam) {
		this.metricMonitorParam = metricMonitorParam;
	}

	/**
	 * 达到阈值的数据临时存储类
	 */
	private static class MetricDataPoint {
		private MonitorState monitorState;
		private PerfMetric perfMetric;
		private Object value;

		private MetricDataPoint(MonitorState monitorState, PerfMetric perfMetric, Object value) {
			super();
			this.monitorState = monitorState;
			this.perfMetric = perfMetric;
			this.value = value;
		}

		private List<TagEntry> getTagList() {
			// 获取指标的详细tag
			Map<String, List<String>> tagMap = perfMetric.getTags();
			if (tagMap == null)
				return new ArrayList<TagEntry>();
			List<TagEntry> tags = new ArrayList<TagEntry>();
			for (Entry<String, List<String>> entry : tagMap.entrySet()) {
				List<String> vs = entry.getValue();
				if (vs == null || vs.size() == 0) {
					tags.add(new TagEntry(entry.getKey(), null));
				} else {
					for (String v : vs) {
						tags.add(new TagEntry(entry.getKey(), v));
					}
				}
			}
			return tags;
		}

		private String getResourceId() {
			Map<String, List<String>> tagMap = perfMetric.getTags();
			if (tagMap == null)
				return null;
			// 内置resourceId，暂不考虑重复的情况
			List<String> resource = tagMap.get("resourceId");
			if (resource == null || resource.size() != 1)
				return null;
			return resource.get(0);
		}
	}

}
