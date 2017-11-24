package uyun.bat.monitor.api.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class Options implements Serializable {
	private static final long serialVersionUID = 1L;

	private static Pattern THRESHOLDS_PATTERN = Pattern.compile("^[^\\s]+ [>,=,<]{1,2} \\d+(\\.\\d+)?.*");

	/**
	 * 告警
	 */
	public static final String ALERT = "alert";
	/**
	 * 警告
	 */
	public static final String WARNING = "warning";
	/**
	 * 提醒
	 */
	public static final String INFO = "info";
	/**
	 * 阈值key列表
	 */
	private static final String[] THRESOLD_KEYS = new String[] { ALERT, WARNING, INFO};
	/**
	 * 恢复事件
	 */
	private String[] eventRecover;

	public String[] getEventRecover() {
		return eventRecover;
	}

	public void setEventRecover(String[] eventRecover) {
		this.eventRecover = eventRecover;
	}

	/**
	 * 监测器阈值 <br>
	 * "alert":last_5m > 10 <br>
	 * "warning":2
	 */
	private Map<String, String> thresholds;

	public Map<String, String> getThresholds() {
		return thresholds;
	}

	public void setThresholds(Map<String, String> thresholds) {
		checkThresholds(thresholds);
		this.thresholds = thresholds;
	}

	/**
	 * 添加阈值
	 */
	public void addThreshold(String key, String value) {
		if (value != null && !THRESHOLDS_PATTERN.matcher(value).matches())
			throw new IllegalArgumentException("Threshold data format exception,value:" + value);

		for (String temp : THRESOLD_KEYS) {
			if (temp.equals(key)) {
				if (thresholds == null)
					thresholds = new HashMap<String, String>();
				thresholds.put(temp, value);
				return;
			}
		}
		throw new IllegalArgumentException("Threshold data format exception,key:" + key);
	}

	private static void checkThresholds(Map<String, String> thresholds) {
		if (thresholds == null || thresholds.isEmpty())
			return;
		for (Entry<String, String> threshold : thresholds.entrySet()) {
			boolean validateKey = false;
			for (String k : THRESOLD_KEYS) {
				if (k.equals(threshold.getKey())) {
					validateKey = true;
					break;
				}
			}
			if (!validateKey)
				throw new IllegalArgumentException("Threshold data format exception,key:" + threshold.getKey());
			if (threshold.getValue() != null&&!"".equals(threshold.getValue()) && !THRESHOLDS_PATTERN.matcher(threshold.getValue()).matches())
				throw new IllegalArgumentException("Threshold data format exception,value:" + threshold.getValue());
		}

	}

}
