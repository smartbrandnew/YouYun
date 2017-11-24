package uyun.bat.monitor.core.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uyun.bat.common.config.Config;

public class EventMonitorParam implements MonitorParam {
	/**
	 * 比较
	 */
	private Comparison comparison;
	/**
	 * 阈值
	 */
	private double threshold;
	/**
	 * 关键词
	 */
	private String keyWords;
	/**
	 * 函数
	 */
	private String aggregator;
	/**
	 * 来源
	 */
	private String[] sources;
	/**
	 * 状态
	 */
	private String[] status;

	private String period;

	private List<TagEntry> tags;
	
	final static boolean isZH = Config.getInstance().isChinese();

	public boolean match(String match, EventData eventData) {
		if (!contains(eventData.getContent(), match) && !contains(eventData.getTitle(), match)) {
			return false;
		}

		if (tags == null || tags.size() == 0)
			return true;
		if (eventData.getTags() == null || eventData.getTags().size() == 0)
			return false;
		for (TagEntry temp : tags) {
			if (!eventData.getTags().contains(temp))
				return false;
		}
		return true;
	}

	public static void main(String[] args) {
		String match="CPU";
		List<TagEntry> tags=new ArrayList<>();
		EventMonitorParam param=new EventMonitorParam();
		tags.add(new TagEntry("host","fengzi"));
		param.setTags(tags);
		EventData eventData=new EventData();
		eventData.setContent("CPU报警");
		tags.add(new TagEntry("host","fengzi"));
		tags.add(new TagEntry("ip","10.1.10.119"));
		eventData.setTags(tags);

		System.out.println(param.match(match,eventData));


	}

	public List<TagEntry> getTags() {
		return tags;
	}

	public void setTags(List<TagEntry> tags) {
		this.tags = tags;
	}

	private boolean contains(String match, String origin) {
		return match.indexOf(origin) >= 0;
	}

	public Comparison getComparison() {
		return comparison;
	}

	public void setComparison(Comparison comparison) {
		this.comparison = comparison;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public String getKeyWords() {
		return keyWords;
	}

	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}

	public String getAggregator() {
		return aggregator;
	}

	public void setAggregator(String aggregator) {
		this.aggregator = aggregator;
	}

	public String[] getSources() {
		return sources;
	}

	public void setSources(String[] sources) {
		this.sources = sources;
	}

	public String[] getStatus() {
		return status;
	}

	public void setStatus(String[] status) {
		this.status = status;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public static double checkThreshold(String threshold) {
		double hold = 0;
		try {
			hold = Double.parseDouble(threshold);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Illegal threshold！");
		}
		return hold;
	}

	public String[] getArrTags() {
		List<String> list = new ArrayList<String>();
		for (TagEntry tag : tags) {
			StringBuilder sb = new StringBuilder();
			sb.append(tag.getKey());
			if (null != tag.getValue() && !"".equals(tag.getValue())) {
				sb.append(":");
				sb.append(tag.getValue());
			}
			list.add(sb.toString());
		}
		return list.toArray(new String[] {});

	}

	@Override
	public Map<String, String> getParamMap() {
		Map<String, String> map = new HashMap<>();
		map.put(MonitorParam.AGGREGATOR, aggregator);
		map.put(MonitorParam.THRESHOLD, String.valueOf(threshold));
		map.put(MonitorParam.COMPARISON, comparison.getCname());
		
		if(isZH)
			map.put(MonitorParam.DURATION, period.replace("m", "分钟").replace("h", "小时"));
		else
			map.put(MonitorParam.DURATION, period.replace("m","minutes").replace("h","hours"));
		map.put(MonitorParam.KEY_WORDS, keyWords);
		return map;
	}
}
