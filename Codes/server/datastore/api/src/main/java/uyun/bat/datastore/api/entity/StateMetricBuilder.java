package uyun.bat.datastore.api.entity;

import java.util.ArrayList;
import java.util.List;

public class StateMetricBuilder {
    private List<StateMetric> metrics = new ArrayList<StateMetric>();

    private StateMetricBuilder()
    {

    }

    public static StateMetricBuilder getInstance()
    {
        return new StateMetricBuilder();
    }

    public StateMetric addMetric(String metricName, String value, long timestamp)
    {
        StateMetric metric = new StateMetric(metricName,value,timestamp);
        this.metrics.add(metric);
        return metric;
    }

    public List<StateMetric> getMetrics()
    {
        return this.metrics;
    }
}
