package uyun.bat.monitor.core.entity;

import java.util.Map;

/**
 * 监测器告警条件类 获取监测器参数列表
 */
public interface MonitorParam {
	// 指标名称或编码
	String METRIC_NAME = "${metric}";
	// 汇聚方法
	String AGGREGATOR = "${aggregator}";
	// 阈值
	String THRESHOLD = "${threshold}";
	// 阈值单位
	String THRESHOLD_UNIT = "${threshold_unit}";
	// 运算符
	String COMPARISON = "${comparison}";
	// 持续时间
	String DURATION = "${duration}";
	// 关键词
	String KEY_WORDS = "${keyWords}";
	//应用
	String APP = "${app}";

	// 指标监测器阈值
	String THRESHOLD_WARN = "${threshold_warn}";
	// 指标监测器阈值单位
	String THRESHOLD_WARN_UNIT = "${threshold_warn_unit}";
	// 指标监测器运算符
	String COMPARISON_WARN = "${comparison_warn}";
	// 指标监测器持续时间
	String DURATION_WARN = "${duration_warn}";

//指标监测器阈值
	String THRESHOLD_INFO = "${threshold_info}";
	// 指标监测器阈值单位
	String THRESHOLD_INFO_UNIT = "${threshold_info_unit}";
	// 指标监测器运算符
	String COMPARISON_INFO = "${comparison_info}";
	// 指标监测器持续时间
	String DURATION_INFO = "${duration_info}";
	
	/**
	 * 获取监测器条件参数列表
	 * 
	 * @return
	 */
	public Map<String, String> getParamMap();
}
