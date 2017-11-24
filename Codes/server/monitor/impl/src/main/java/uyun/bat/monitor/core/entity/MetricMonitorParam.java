package uyun.bat.monitor.core.entity;

import uyun.bat.common.config.Config;
import uyun.bat.monitor.api.entity.Options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MetricMonitorParam implements MonitorParam {
	private String metric;
	/**
	 * 聚合获得时间序列数据avg:system.cpu.system
	 */
	private Aggregator aggregator;
	/**
	 * 阈值查询
	 */
	private List<Threshold> thresholds;
	/**
	 * 分组tag key
	 */
	private List<String> groups;

	private List<TagEntry> tags;

	final static boolean isZH=Config.getInstance().isChinese();
	
	public boolean match(MetricData metricData) {
		if (!metric.equals(metricData.getMetric()))
			return false;
		// 匹配tag

		// 监测器没有tag，则只要指标名匹配都符合
		if (tags == null || tags.size() == 0)
			return true;
		// 监测器有tag但是指标数据没tag
		if (metricData.getTags() == null || metricData.getTags().size() == 0)
			return false;
		// 指标数据的tag是否包含监测器的tag
		for (TagEntry monitorTag : tags) {
			boolean isMatach = false;
			for (TagEntry metricTag : metricData.getTags()) {
				if (matchTagEntry(monitorTag, metricTag)) {
					isMatach = true;
					break;
				}
			}
			if (!isMatach)
				return false;
		}
		return true;
	}

	/**
	 * 匹配指标tag
	 */
	private boolean matchTagEntry(TagEntry monitorTag, TagEntry metricTag) {
		if (!monitorTag.getKey().equals(metricTag.getKey()))
			return false;
		if (monitorTag.getValue() == null || monitorTag.getValue().length() == 0) {
			if (metricTag.getValue() == null || metricTag.getValue().length() == 0)
				return true;
			return false;
		} else {
			if (metricTag.getValue() == null || metricTag.getValue().length() == 0) {
				return false;
			} else {
				// 由于同tagk的tagv会用逗号相加,故判断下里面是否包含本tag
				if (metricTag.getValue().indexOf(',') != -1) {
					String[] metricTagvs = metricTag.getValue().split(",");
					String[] monitorTagvs = monitorTag.getValue().split(",");
					if (metricTagvs.length < monitorTagvs.length)
						return false;
					for (String monitorTagv : monitorTagvs) {
						boolean isContain = false;
						for (String metricTagv : metricTagvs) {
							if (metricTagv.equals(monitorTagv)) {
								isContain = true;
								break;
							}
						}
						if (!isContain)
							return false;
					}
					return true;
				} else {
					return monitorTag.getValue().equals(metricTag.getValue());
				}
			}
		}
	}

	public List<Threshold> getThresholds() {
		return thresholds;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public List<TagEntry> getTags() {
		return tags;
	}

	public void setTags(List<TagEntry> tags) {
		this.tags = tags;
	}

	public Aggregator getAggregator() {
		return aggregator;
	}

	public void setAggregator(Aggregator aggregator) {
		this.aggregator = aggregator;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	public void setThresholds(Options options) {
		if (options != null && options.getThresholds() != null) {
			List<Threshold> ts = new ArrayList<Threshold>();
			// last_5m > 10.0或last_5m > 10.0 MB
			String condition = options.getThresholds().get(Options.ALERT);
			Threshold t = Threshold.parseToThreshold(Options.ALERT, condition);
			if (t != null)
				ts.add(t);
			condition = options.getThresholds().get(Options.WARNING);
			t = Threshold.parseToThreshold(Options.WARNING, condition);
			if (t != null)
				ts.add(t);
			condition = options.getThresholds().get(Options.INFO);
			t = Threshold.parseToThreshold(Options.INFO, condition);
			if (t != null)
				ts.add(t);
			this.thresholds = ts;
		}
	}

	/**
	 * 根据时间窗格对阈值列表进行分组
	 */
	public Map<String, List<Threshold>> getPeriodThresholdMap() {
		Map<String, List<Threshold>> ps = new HashMap<String, List<Threshold>>();
		for (Threshold t : thresholds) {
			List<Threshold> temp = ps.get(t.period);
			if (temp == null) {
				temp = new ArrayList<Threshold>();
				ps.put(t.period, temp);
			}
			temp.add(t);
		}

		return ps;
	}

	public static class Threshold {
		private static Pattern pattern = Pattern.compile(" ");
		private static Pattern pattern5 = Pattern.compile("_");

		private String threshold;
		private String period;
		private Comparison comparison;
		private double value;
		private String unit;

		public String getThreshold() {
			return threshold;
		}

		public Comparison getComparison() {
			return comparison;
		}

		public String getPeriod() {
			return period;
		}

		public double getValue() {
			return value;
		}

		public String getUnit() {
			return unit;
		}

		private static Threshold parseToThreshold(String threshold, String condition) {
			if (condition == null)
				return null;
			String[] conditions = pattern.split(condition, 0);
			String[] tempArray = pattern5.split(conditions[0], 0);
			Threshold t = new Threshold();
			t.period = tempArray[1];
			t.comparison = Comparison.checkByCode(conditions[1]);
			t.value = Double.parseDouble(conditions[2]);
			t.threshold = threshold;
			t.unit = conditions.length==3?"":conditions[3];
			return t;
		}

	}

	@Override
	public Map<String, String> getParamMap() {
		Map<String, String> map = new HashMap<>();
		map.put(MonitorParam.METRIC_NAME, metric);
		
		// 指标监测器alert和warning两个阈值
		map.put(MonitorParam.THRESHOLD, String.valueOf(thresholds.get(0).getValue()));
		map.put(MonitorParam.THRESHOLD_UNIT,thresholds.get(0).getUnit());
		map.put(MonitorParam.THRESHOLD_WARN, String.valueOf(thresholds.get(1).getValue()));
		map.put(MonitorParam.THRESHOLD_WARN_UNIT,thresholds.get(1).getUnit());
		if (thresholds.size() > 2) {
			map.put(MonitorParam.THRESHOLD_INFO, String.valueOf(thresholds.get(2).getValue()));
			map.put(MonitorParam.THRESHOLD_INFO_UNIT, thresholds.get(2).getUnit());
		}
		if (isZH) {
			map.put(MonitorParam.AGGREGATOR, aggregator.getName());
			map.put(MonitorParam.COMPARISON, thresholds.get(0).getComparison().getCname());
			map.put(MonitorParam.COMPARISON_WARN, thresholds.get(1).getComparison().getCname());
			if (thresholds.size() > 2) {
				map.put(MonitorParam.COMPARISON_INFO, thresholds.get(2).getComparison().getCname());
			}
			map.put(MonitorParam.DURATION, thresholds.get(0).getPeriod().replace("m", "分钟").replace("h", "小时"));
			map.put(MonitorParam.DURATION_WARN, thresholds.get(1).getPeriod().replace("m", "分钟").replace("h", "小时"));
			if (thresholds.size() > 2) {
				map.put(MonitorParam.DURATION_INFO, thresholds.get(2).getPeriod().replace("m", "分钟").replace("h", "小时"));
			}
		} else {
			map.put(MonitorParam.AGGREGATOR, " " + aggregator.getCode());
			map.put(MonitorParam.COMPARISON, thresholds.get(0).getComparison().getName());
			map.put(MonitorParam.COMPARISON_WARN, thresholds.get(1).getComparison().getName());
			if (thresholds.size() > 2) {
				map.put(MonitorParam.COMPARISON_INFO, thresholds.get(2).getComparison().getName());
			}
			map.put(MonitorParam.DURATION, thresholds.get(0).getPeriod().replace("m", "minutes").replace("h", "hours"));
			map.put(MonitorParam.DURATION_WARN, thresholds.get(1).getPeriod().replace("m", "minutes").replace("h", "hours"));
			if (thresholds.size() > 2) {
				map.put(MonitorParam.DURATION_INFO, thresholds.get(2).getPeriod().replace("m", "minutes").replace("h", "hours"));
			}
		}
		return map;
	}
}
