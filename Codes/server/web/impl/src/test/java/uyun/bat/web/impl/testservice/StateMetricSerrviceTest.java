package uyun.bat.web.impl.testservice;

import uyun.bat.datastore.api.service.StateMetricService;

import java.util.ArrayList;
import java.util.List;

public class StateMetricSerrviceTest implements StateMetricService{

    @Override
    public List<String> getStateMetrics(String tenantId) {
        List<String> list=new ArrayList<>();
        list.add("mysql.can_connect");
        list.add("redis.can_connect");
        return list;
    }
}
