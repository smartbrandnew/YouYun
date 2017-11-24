package uyun.bat.datastore.api.entity;

import java.util.ArrayList;
import java.util.List;

public class PerfMetricBuilder {
	private List<PerfMetric> metrics = new ArrayList<PerfMetric>();

	private PerfMetricBuilder()
	{

	}

	public static PerfMetricBuilder getInstance()
	{
		return new PerfMetricBuilder();
	}

	public PerfMetric addMetric(String metricName)
	{
		PerfMetric metric = new PerfMetric(metricName);
		this.metrics.add(metric);
		return metric;
	}

	public List<PerfMetric> getMetrics()
	{
		return this.metrics;
	}

}
