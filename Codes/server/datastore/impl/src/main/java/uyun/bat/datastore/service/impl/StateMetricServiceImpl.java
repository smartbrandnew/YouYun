package uyun.bat.datastore.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import uyun.bat.datastore.api.service.StateMetricService;
import uyun.bat.datastore.logic.StateMetricLogic;

import java.util.List;

public class StateMetricServiceImpl implements StateMetricService {

    @Autowired
    private StateMetricLogic stateMetricLogic;

    @Override
    public List<String> getStateMetrics(String tenantId) {
        return stateMetricLogic.getStateMetrics(tenantId);
    }
}
