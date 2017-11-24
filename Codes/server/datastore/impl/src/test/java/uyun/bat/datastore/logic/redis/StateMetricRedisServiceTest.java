package uyun.bat.datastore.logic.redis;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import uyun.bat.datastore.Startup;
import uyun.bat.datastore.entity.StateMetricResource;

import java.util.List;

import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StateMetricRedisServiceTest {

    private static StateMetricRedisService stateMetricRedisService = (StateMetricRedisService) Startup.getInstance().getBean("stateMetricRedisService");

    private static final String TENANT_ID = "23345678910111213141516171819203";
    private static final String RESOURCE_ID = "33345678910111213141516171819204";

    private static final String[] metircNames=new String[]{"mysql.can_connect","redis.can_connect"};

    @Test
    public void test1Save(){
        stateMetricRedisService.addMetricNames(metircNames,RESOURCE_ID,TENANT_ID);
    }

    @Test
    public void test2GetStateAsyncMetricNames(){
        List<String> reses=stateMetricRedisService.getStateAsyncMetricNames();
        //assertTrue(reses.size()==1);
        //assertTrue(reses.get(0).equals(RESOURCE_ID));
    }

    @Test
    public void test3GetResourceMetric(){
        StateMetricResource resource=stateMetricRedisService.getResourceMetric(RESOURCE_ID);
        assertTrue(resource.getTenantId().equals(TENANT_ID));
    }

    @Test
    public void test4GetByRes(){
        List<String> metricNames=stateMetricRedisService.getByResId(RESOURCE_ID);
        assertTrue(metricNames.size()==2);
    }

    @Test
    public void test5DeleteByResId(){
        stateMetricRedisService.deleteByResId(RESOURCE_ID);
        List<String> reses=stateMetricRedisService.getStateAsyncMetricNames();
        List<String> metricNames=stateMetricRedisService.getByResId(RESOURCE_ID);
        /*assertTrue(reses.size()==0);
        assertTrue(metricNames.size()==0);*/
    }
}
