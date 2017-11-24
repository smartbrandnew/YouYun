package uyun.bat.web.impl.service.rest.state;

import org.junit.Test;
import uyun.bat.web.impl.testservice.StartService;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class StateRESTServiceTest extends StartService{

    StateRESTService stateRESTService=new StateRESTService();

    public static String tenantId="94baaadca64344d2a748dff88fe7159e";

    @Test
    public void testgetStateApps(){
        List<String> states=stateRESTService.getStateApps(tenantId);
        assertTrue(states.size()==2);
        assertTrue(states.get(0).equals("mysql:mysql.can_connect"));
    }

    @Test
    public void testgetTagsByState(){
        List<String> tags= stateRESTService.getTagsByState(tenantId,"mysql");
        assertTrue(null==tags);
    }

}
