package uyun.bat.event.impl.logic;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import uyun.bat.event.api.entity.Event;
import uyun.bat.event.api.entity.EventFault;
import uyun.bat.event.api.entity.EventServerityType;
import uyun.bat.event.api.entity.EventSourceType;
import uyun.bat.event.api.logic.EventLogic;
import uyun.bat.event.impl.Startup;
import uyun.bat.event.impl.logic.redis.EventRedisService;
import uyun.bat.event.impl.util.DateUtil;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.assertTrue;
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EventRedisServiceTest {

    private static EventRedisService eventRedisService=(EventRedisService) Startup.getInstance().getBean("eventRedisService");

    private static EventLogic eventLogic = (EventLogic) Startup.getInstance().getBean("eventLogic");

    public static String tenantId = "21111111164344d2a748dff88fe7159e";
    public static String monitorId = "31111111164344d2a748dff88fe7159e";
    public static String resId1 = "11111111164344d2a748dff88fe7159e";
    public static String identity = "12341231232";

    @Test
    public void testFaultExpDay(){
        create();
        EventFault fault=eventRedisService.getByTenantIdAndIdentity(tenantId,resId1,identity);
        assertTrue(null!=fault);
        assertTrue(fault.getRelateCount()==1);
    }

    @Test
    public void testzClearData(){
        boolean success=eventRedisService.deleteByTenantIdAndIdentity(tenantId,resId1,identity);
        assertTrue(success);
    }

    private void create(){
        String title = "test";
        short warnning = EventServerityType.WARNING.getKey();
        short critical = EventServerityType.ERROR.getKey();

        Date occurTime1= DateUtil.getDateAdd(new Date(),-9);
        Event event1 = new Event(UUIDTypeHandler.createUUID(), occurTime1, warnning, resId1, title, title,
                EventSourceType.DATADOG_AGENT.getKey(), tenantId, monitorId, identity);
        event1.setNow(occurTime1.getTime());

        Date occurTime2= DateUtil.getDateAdd(new Date(),-8);
        Event event2 = new Event(UUIDTypeHandler.createUUID(), occurTime2, warnning, resId1, title,
                title, EventSourceType.DATADOG_AGENT.getKey(), tenantId, monitorId, identity);
        event2.setNow(occurTime2.getTime());

        Date occurTime3=  new Date();
        Event event3 = new Event(UUIDTypeHandler.createUUID(), occurTime3, critical, resId1, title,
                title, EventSourceType.DATADOG_AGENT.getKey(), tenantId, monitorId, identity);

        List<Event> events=new ArrayList<>();
        events.add(event1);events.add(event2);
        events.add(event3);
        eventLogic.create(events);
    }

}
