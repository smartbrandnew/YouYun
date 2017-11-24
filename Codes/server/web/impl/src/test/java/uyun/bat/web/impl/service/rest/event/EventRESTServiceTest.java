package uyun.bat.web.impl.service.rest.event;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uyun.bat.web.api.event.envity.EventGraphDataVO;
import uyun.bat.web.api.event.envity.MineEvent;
import uyun.bat.web.impl.Startup;
import uyun.bat.web.impl.common.service.ServiceManager;
import uyun.bat.web.impl.testservice.EventServiceTest;
import uyun.bat.web.impl.testservice.MonitorServiceTest;
import uyun.bat.web.impl.testservice.ResourceServiceTest;
import uyun.bat.web.impl.testservice.StartService;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

public class EventRESTServiceTest extends StartService{
	EventRESTService eventREST=new EventRESTService();
	
	public static String tenantId="94baaadca64344d2a748dff88fe7159e";
    public static String monitorId= UUIDTypeHandler.createUUID();;
    public static String resId=UUIDTypeHandler.createUUID();;


	@Test
	public void test1SearchEvent() {
		int page=1;
        int rp=10;
        String searchValue="";
       /* String beginTime= "2016-09-20 15:00:00";
        String  endTime= "2016-09-31 17:00:00";*/
        
        int granularity=30;
		long beginTime=System.currentTimeMillis();
		long endTime=System.currentTimeMillis()+1;
		MineEvent mine = eventREST.searchEvent(tenantId, page, rp, searchValue, null, beginTime, endTime, granularity);
		assertTrue(mine!=null&&mine.getRows().get(0).getId().equals("1234"));
	}

	@Test
	public void test2SearchEventGraphData() {
        String searchValue="";
        /*String beginTime= "2016-09-20 15:00:00";
        String  endTime= "2016-09-31 17:00:00";*/
        long beginTime = 1469862927000L;
    	long endTime = 1472454927000L;
        int granularity=30;
        EventGraphDataVO evo=eventREST.searchEventGraphData(tenantId, searchValue, beginTime, endTime, granularity);
        assertTrue(evo!=null&&!evo.getGraphs().isEmpty());
	}

	@Test
	public void test3GetEventsByFaultId() {
		String eventId="123";
		String faultId="123";
		int current=1;
		int pageSize=10;
		MineEvent me=eventREST.getEventsByFaultId(tenantId, eventId, faultId, current, pageSize);
		assertTrue(me!=null&&me.getRows().get(0).getId().equals("1234"));
	}

	@Test
	public void test4getEventCount() {
		//String beginTime= "2016-09-20 15:00:00";
		long beginTime = 1494398743228L;
		assertTrue(eventREST.getEventCount(tenantId, beginTime)==0);
	}

}
