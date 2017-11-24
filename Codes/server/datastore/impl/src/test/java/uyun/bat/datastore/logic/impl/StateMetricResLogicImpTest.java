package uyun.bat.datastore.logic.impl;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import uyun.bat.datastore.Startup;
import uyun.bat.datastore.entity.StateMetricResource;
import uyun.bat.datastore.logic.StateMetricLogic;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StateMetricResLogicImpTest {

    private static StateMetricLogic stateMetricLogic = (StateMetricLogic) Startup.getInstance().getBean("stateMetricLogic");

    private static final String TENANT_ID = "23345678910111213141516171819203";
    private static final String RESOURCE_ID = "33345678910111213141516171819204";

    @Test
    public void test1Insert(){
        StateMetricResource resource=new StateMetricResource();
        resource.setResourceId(RESOURCE_ID);
        resource.setTenantId(TENANT_ID);
        List<String> metricNames=new ArrayList<>();
        metricNames.add("mysql.can_connect");
        metricNames.add("redis.can_connect");
        resource.setMetricNames(metricNames);
        stateMetricLogic.insert(resource);
        List<String> states=stateMetricLogic.getStateMetricsByResId(TENANT_ID,RESOURCE_ID);
        assertTrue(metricNames.size()==states.size());
    }

    @Test
    public void test2Delete(){
        stateMetricLogic.deleteByResId(TENANT_ID,RESOURCE_ID);
        List<String> states=stateMetricLogic.getStateMetricsByResId(TENANT_ID,RESOURCE_ID);
        assertTrue(states.isEmpty());
    }

}
