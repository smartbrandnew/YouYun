package uyun.bat.event.impl.service;

import org.junit.Test;
import uyun.bat.event.api.entity.*;
import uyun.bat.event.api.service.EventService;
import uyun.bat.event.impl.Startup;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EventServiceTest {

    private static EventService eventService = (EventService) Startup.getInstance().getBean("eventService");

    public static String tenantId="xxbaaadca64344d2a748dff88fe7159e";
    public static String monitorId= UUIDTypeHandler.createUUID();
    public static String resId=UUIDTypeHandler.createUUID();


    @Test
    public void testCreate(){
        Event event=new Event();
        event.setOccurTime(new Timestamp(System.currentTimeMillis()));
        event.setTenantId(tenantId);
        event.setResId(resId);
        event.setMsgTitle("标题是什么");
        event.setMsgContent("内容是什么");
        event.setSourceType(EventSourceType.MONITOR.getKey());
        event.setServerity(EventServerityType.WARNING.getKey());
        event.setAgentId("44444444");
        event.setMonitorId(monitorId);
        List<EventTag> eventTags=new ArrayList<EventTag>();
        EventTag eventTag=new EventTag();
        eventTag.setTagk("key");
        eventTag.setTagv("value");
        eventTag.setTenantId(tenantId);
        eventTags.add(eventTag);
        event.setEventTags(eventTags);
        event.setId(UUIDTypeHandler.createUUID());
        eventService.create(event);
    }

    @Test
    public void testSearchEvent() throws IOException {
        int page=1;
        int rp=10;
        String searchValue="";
        /*String beginTime= "2016-05-31 15:00:00";
        String  endTime= "2016-06-31 17:00:00";*/
        long beginTime = 1464678000000L;
        long endTime = 1467363600000L;
        int granularity=30;
        eventService.searchEvent(tenantId,page,rp,searchValue,null,beginTime,endTime,granularity);
    }

    @Test
    public void testSearchEventGraphData() throws IOException {
        String searchValue="";
        /*String beginTime= "2016-05-16 11:00:00";
        String  endTime= "2016-05-16 14:00:00";*/
        long beginTime = 1463367600000L;
        long endTime = 1463378400000L;
        int granularity=30;
        eventService.searchEventGraphData(tenantId,searchValue,beginTime,endTime,granularity);
    }

}
