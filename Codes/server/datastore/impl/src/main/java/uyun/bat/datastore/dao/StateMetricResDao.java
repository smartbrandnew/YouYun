package uyun.bat.datastore.dao;

import uyun.bat.datastore.entity.StateMetricResource;

import java.util.List;

public interface StateMetricResDao {


    List<String> getResIdInId(List<String> list);

    void batchUpdate(List<StateMetricResource> update);

    void batchInsert(List<StateMetricResource> insert);

    List<StateMetricResource> getStateMetrics(String tenantId);

    void delete(String tenantId, String resourceId);

    List<StateMetricResource> getStateMetricsByResId(String tenantId, String resId);
}
