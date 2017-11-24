package uyun.bat.datastore.api.mq;

import java.io.Serializable;
import java.util.List;

import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.StateMetric;

public class StateMetricData implements Serializable {

    /**
     * 资源
     */
    private Resource resource;

    private List<StateMetric> stateMetrics;

    public StateMetricData() {
    }

    public StateMetricData(Resource resource, List<StateMetric> stateMetrics) {
        this.resource=resource;
        this.stateMetrics=stateMetrics;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public List<StateMetric> getStateMetrics() {
        return stateMetrics;
    }

    public void setStateMetrics(List<StateMetric> stateMetrics) {
        this.stateMetrics = stateMetrics;
    }
}
