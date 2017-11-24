package uyun.bat.datastore.logic;

import uyun.bat.datastore.api.mq.StateMetricData;
import uyun.bat.datastore.entity.StateMetricResource;

import java.util.List;

public interface StateMetricLogic {
    void insertStateMetric(StateMetricData stateMetricData) ;

    List<String> getStateMetrics(String tenantId);

    void deleteByResId(String tenantId, String resourceId);

    List<String> getStateMetricsByResId(String tenantId,String resId);

    void insert(StateMetricResource stateMetricResource);
}
