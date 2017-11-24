package uyun.bat.datastore.api.service;

import java.util.List;

public interface StateMetricService {

    /**
     * 根据租户ID获取状态指标名称
     * @param tenantId
     * @return
     */
    List<String> getStateMetrics(String tenantId);

}
