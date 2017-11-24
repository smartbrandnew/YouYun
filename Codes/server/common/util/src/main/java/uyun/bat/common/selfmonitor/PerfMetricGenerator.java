package uyun.bat.common.selfmonitor;

import java.util.List;

public class PerfMetricGenerator {

	public static PerfMetricVO generatePerfMetric(String metricname, List<String> tags, double value) {
		PerfMetricVO vo = new PerfMetricVO();
		vo.setMetric(metricname);
		vo.setTags(tags);
		vo.setTimestamp(System.currentTimeMillis());
		vo.setValue(value);
		return vo;
	}
}
