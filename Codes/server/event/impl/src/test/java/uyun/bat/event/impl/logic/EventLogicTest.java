package uyun.bat.event.impl.logic;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import uyun.bat.event.api.entity.*;
import uyun.bat.event.api.logic.EventLogic;
import uyun.bat.event.impl.Startup;
import uyun.bat.event.impl.util.DateUtil;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EventLogicTest {

	private static EventLogic eventLogic = (EventLogic) Startup.getInstance().getBean("eventLogic");

	public static String tenantId = "21111111164344d2a748dff88fe7159e";
	public static String monitorId ="31111111164344d2a748dff88fe7159e";

	private void create(List<Event> events) {
		for (Event e : events) {
			eventLogic.create(e);
		}
	}

	public int currentPage = 1;
	public int pageSize = 10;

	@Test
	public void testaClear() {
		Date begin = DateUtil.str2Day("2000-01-01");
		Date end = DateUtil.str2Day("2000-02-01");
		eventLogic.deleteByTime(begin, end);
	}

	@Test
	public void testbCreate() {
		create();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testcEventGraphData() throws InterruptedException {
		int granularity = 30;
		/*String beginSch = "2000-01-01 00:00:00";
		String endSch = "2000-01-01 00:30:00";*/
		long beginSch = 946656000000L;
		long endSch = 946657800000L;
		String searchValue = "";
		EventGraphData data = eventLogic.searchEventGraphData(tenantId, searchValue, beginSch, endSch, granularity);
		assertTrue(data.getGraphs().size() == granularity);

		PageEvent page = eventLogic.searchEvent(tenantId, currentPage, pageSize, searchValue, "", beginSch, endSch,
				granularity);
		assertTrue(page.getTotal() == 1);
		Event event = page.getRows().get(0);

		EventQuery query = new EventQuery();
		query.setEventId(event.getId());
		query.setTenantId(tenantId);
		query.setFaultId(event.getFaultId());
		PageEvent page2 = eventLogic.getEventsByFaultId(tenantId, event.getId(), event.getFaultId(), currentPage, pageSize);
		assertTrue(page2.getTotal() == 5);
	}

	@Test
	public void testdSearchPageEvent() throws InterruptedException {
		/*String beginSch = "2000-01-01 00:00:00";
		String endSch = "2000-01-01 01:40:00";*/
		long beginSch = 946656000000L;
		long endSch = 946662000000L;
		EventQuery schQuery = new EventQuery();
		schQuery.setTenantId(tenantId);
		schQuery.setBeginTime(new Date(beginSch));
		schQuery.setEndTime(new Date(endSch));
		schQuery.setServerity(EventServerityType.WARNING.getKey());
		String searchValue = "";
		String serverity = EventServerityType.WARNING.getKey() + "";
		int granularity = 30;
		PageEvent pageEvent = eventLogic.searchEvent(tenantId, currentPage, pageSize, searchValue, serverity, beginSch,
				endSch, granularity);
		assertTrue(pageEvent.getTotal() == 2);
		List<Event> events = pageEvent.getRows();
		assertTrue(events.get(0).getServerity() == EventServerityType.WARNING.getKey());
		assertTrue(events.get(0).getEventMeta().getMeta().size() == 2);
		assertTrue(events.get(1).getEventMeta().getMeta().size() == 5);
		assertTrue(events.get(1).getEventMeta().getMeta().get(3).intValue() == EventServerityType.WARNING.getKey());

	}

	public static String resId1 = "06f5af1b7d754ff098e81aa910ce0b60";
	public static String resId2 = "06f5af1b7d754ff098e81aa910ce0b61";
	public static String eventId = "1234af1b7d754ff098e81aa910ce0b61";
	public static String tags = "app:tomcat";

	private void create() {
		String title = "端口异常下线";
		String title1 = "connect error";
		String identity1 = "12341231232";
		short warnning = EventServerityType.WARNING.getKey();
		short critical = EventServerityType.ERROR.getKey();
		short ok = EventServerityType.SUCCESS.getKey();

		//        事件流1
		String occurTime1 = "2000-01-01 00:03:00";
		Event event1 = new Event(eventId, DateUtil.str2Time(occurTime1), warnning, resId1, title, title,
				EventSourceType.DATADOG_AGENT.getKey(), tenantId, monitorId, identity1);
		List<EventTag> eventTags = new ArrayList<EventTag>();
		String[] tempArr = tags.split(";");
		for (String tag : tempArr) {
			String[] arr = tag.split(":");
			eventTags.add(new EventTag(tenantId, arr[0], arr[1]));
		}
		event1.setEventTags(eventTags);
		event1.setNow(DateUtil.str2Time(occurTime1).getTime());

		String occurTime2 = "2000-01-01 00:05:59";
		Event event2 = new Event(UUIDTypeHandler.createUUID(), DateUtil.str2Time(occurTime2), warnning, resId1, title1,
				title, EventSourceType.DATADOG_AGENT.getKey(), tenantId, monitorId, identity1);
		eventTags = new ArrayList<EventTag>();
		eventTags.add(new EventTag(tenantId, "host", "202.101.178.39"));
		eventTags.add(new EventTag(tenantId, "app", "mysql"));
		event2.setEventTags(eventTags);
		event2.setNow(DateUtil.str2Time(occurTime2).getTime());

		String occurTime3 = "2000-01-01 00:08:00";
		Event event3 = new Event(UUIDTypeHandler.createUUID(), DateUtil.str2Time(occurTime3), critical, resId1, title1,
				title1, EventSourceType.DATADOG_AGENT.getKey(), tenantId, monitorId, identity1);
		eventTags = new ArrayList<EventTag>();
		eventTags.add(new EventTag(tenantId, "host", "202.101.178.39"));
		eventTags.add(new EventTag(tenantId, "app", "tomcat"));
		event3.setEventTags(eventTags);
		event3.setNow(DateUtil.str2Time(occurTime3).getTime());

		String occurTime4 = "2000-01-01 00:32:00";
		Event event4 = new Event(UUIDTypeHandler.createUUID(), DateUtil.str2Time(occurTime4), critical, resId1, title1,
				title, EventSourceType.DATADOG_AGENT.getKey(), tenantId, monitorId, identity1);
		eventTags = new ArrayList<EventTag>();
		eventTags.add(new EventTag(tenantId, "host", "202.101.178.40"));
		event4.setEventTags(eventTags);
		event4.setNow(DateUtil.str2Time(occurTime4).getTime());

		String occurTime5 = "2000-01-01 01:20:00";
		Event event5 = new Event(UUIDTypeHandler.createUUID(), DateUtil.str2Time(occurTime5), ok, resId1, title, title,
				EventSourceType.DATADOG_AGENT.getKey(), tenantId, monitorId, identity1);
		eventTags = new ArrayList<EventTag>();
		eventTags.add(new EventTag(tenantId, "host", "202.101.178.41"));
		event5.setEventTags(eventTags);
		event5.setNow(DateUtil.str2Time(occurTime5).getTime());

//		事件流2
		String identity2 = "12341231232sdfsdfc";
		String occurTime6 = "2000-01-01 01:34:00";
		Event event6 = new Event(UUIDTypeHandler.createUUID(), DateUtil.str2Time(occurTime6), warnning, resId2, title1,
				title1, EventSourceType.DATADOG_AGENT.getKey(), tenantId, monitorId, identity2);
		eventTags = new ArrayList<EventTag>();
		eventTags.add(new EventTag(tenantId, "host", "202.101.178.39"));
		event6.setEventTags(eventTags);
		event6.setNow(DateUtil.str2Time(occurTime6).getTime());

		String occurTime7 = "2000-01-01 02:02:00";
		Event event7 = new Event(UUIDTypeHandler.createUUID(), DateUtil.str2Time(occurTime7), ok, resId2, title, title,
				EventSourceType.DATADOG_AGENT.getKey(), tenantId, monitorId, identity2);
		eventTags = new ArrayList<EventTag>();
		eventTags.add(new EventTag(tenantId, "host", "202.101.178.43"));
		eventTags.add(new EventTag(tenantId, "app", "nginx"));
		event7.setEventTags(eventTags);
		event7.setNow(DateUtil.str2Time(occurTime7).getTime());

		List<Event> events = new ArrayList<>();
		events.add(event1);
		events.add(event2);
		events.add(event3);
		events.add(event4);
		events.add(event5);
		events.add(event6);
		events.add(event7);
		create(events);
		EVENT_COUNT = events.size();
	}

	public static int EVENT_COUNT;

	@Test
	public void testeMonitorConditionStisfied() throws IOException {
		Short[] sourceType = {};
		Short[] serverity = { 2 };
		String keyWords = "端口;异常;下线";
		String beginSch = "2000-01-01 00:00:00";
		String endSch = "2000-01-01 02:00:00";
		Date beginTime = DateUtil.str2Time(beginSch);
		Date endTime = DateUtil.str2Time(endSch);
		List<EventMonitorData> eventMonitorDatas = eventLogic.queryMatchedMonitorData(tenantId, sourceType, serverity,
				keyWords, null, beginTime, endTime);
		assertTrue(eventMonitorDatas.size() == 1);
		assertTrue(eventMonitorDatas.get(0).getCount() == 2);

	}

	@Test
	public void testeGetEventsByFaultId() {
		Event event = eventLogic.getEventById(eventId);
		PageEvent pageEvent = eventLogic.getEventsByFaultId(tenantId, event.getId(), event.getFaultId(), 1, 100);
		assertTrue(pageEvent.getTotal() == 5);
	}

	@Test
	public void testhGetTagsByEventId() {
		List<String> tag = eventLogic.getTagsByEventId(eventId);
		if (tag.size() != 0)
			assertTrue((tag.get(0)).equals(tags));
	}

	@Test
	public void testiGetLatestEventCount() {
		Date begin = DateUtil.str2Day("2000-01-01");
		Date end = DateUtil.str2Day("2000-02-01");
		int count = eventLogic.getEventCount(tenantId, begin, end);
		assertTrue(count == EVENT_COUNT);
	}

	@Test
	public void testjSearchEvents(){
		Date begin = DateUtil.str2Day("2000-01-01");
		Date end = DateUtil.str2Day("2000-02-01");
		MinePageEvent pageEvent=eventLogic.searchEvent(tenantId,1,100,resId1,begin,end);
		assertTrue(pageEvent.getCount()==5);
	}


	@Test
	public void testkDelete(){
		Event event=eventLogic.getEventById(eventId);
		String faultId=event.getFaultId();
		assertTrue(eventLogic.delete(tenantId,resId1));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		EventFault fault=eventLogic.getFaultById(faultId);
		Event event2=eventLogic.getEventById(eventId);
		assertTrue(fault.getFaultId()==null);
		assertTrue(event2.getId()==null);
	}

	@Test
	public void testzClearData() {
		Date begin = DateUtil.str2Day("2000-01-01");
		Date end = DateUtil.str2Day("2000-02-01");
		eventLogic.deleteByTime(begin, end);
	}

	@Test
	public void testzGetSpanTime() {
		eventLogic.getEventSpanTime();
	}


}
