package uyun.bat.web.impl.testservice;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uyun.bat.event.api.entity.*;
import uyun.bat.event.api.service.EventService;

public class EventServiceTest implements EventService{

	@Override
	public Event create(Event event) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PageEvent searchEvent(String tenantId, int page, int rp, String searchValue, String serverity,
			long beginTime, long endTime, Integer granularity) {
		// TODO Auto-generated method stub
		PageEvent pe=new PageEvent();
		Event e=new Event();
		e.setOccurTime(null);
		e.setMsgTitle(tenantId);
		e.setMsgContent(serverity);
		e.setServerity((short)123);
		e.setRelateCount(1);
		e.setFirstRelateTime(null);
		e.setFaultId(tenantId);
		e.setMonitorId("1234");
		e.setResId("1234");
		e.setId("1234");
		EventMeta eventMeta = new EventMeta();
		eventMeta.setTotal(2);
		e.setEventMeta(eventMeta);
		List<Event> rows=new ArrayList<Event>();
		rows.add(e);
		pe.setRows(rows);
		return pe;
	}

	@Override
	public EventGraphData searchEventGraphData(String tenantId, String searchValue, long beginTime, long endTime,
			int granularity) {
		// TODO Auto-generated method stub
		EventGraphData e=new EventGraphData();
		List<EventGraphBuild> graphs=new ArrayList<EventGraphBuild>();
		EventGraphBuild egb=new EventGraphBuild();
		EventAlert alerts=new EventAlert();
		alerts.setCritical(1);
		alerts.setInfo(2);
		alerts.setSuccess(3);
		alerts.setWarnning(4);
		egb.setAlerts(alerts);
		egb.setTime(new Date());
		egb.setTotal(1);
		graphs.add(egb);
		e.setGraphs(graphs);
		e.setBeginTime(new Date());
		e.setDiffTime(granularity);
		e.setEndTime(new Date());
		return e;
	}

	@Override
	public long create(List<Event> events) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public PageEvent getEventsByFaultId(String tenantId, String id, String faultId, int current, int pageSize) {
		// TODO Auto-generated method stub
		PageEvent pe=new PageEvent();
		Event e=new Event();
		e.setOccurTime(null);
		e.setMsgTitle(tenantId);
		e.setMsgContent("123");
		e.setServerity((short)123);
		e.setRelateCount(1);
		e.setFirstRelateTime(null);
		e.setFaultId(tenantId);
		e.setMonitorId("1234");
		e.setResId("1234");
		e.setId("1234");
		EventMeta eventMeta = new EventMeta();
		eventMeta.setTotal(2);
		eventMeta.setCurrentPage(2);
		eventMeta.setMeta(null);
		e.setEventMeta(eventMeta);
		List<Event> rows=new ArrayList<Event>();
		rows.add(e);
		pe.setRows(rows);
		pe.setMetas(eventMeta);
		return pe;
	}

	

	@Override
	public List<String> getTagsByEventId(String eventId) {
		// TODO Auto-generated method stub
		return null;
	}

	

	@Override
	public List<EventMonitorData> queryMatchedMonitorData(String tenantId, Short[] sourceTypes, Short[] serveritys,
			String keyWords, String[] tags, Date beginTime, Date endTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getEventCount(String tenantId, Date begin, Date end) {
		// TODO Auto-generated method stub
		
		return 0;
	}

	@Override
	public MinePageEvent searchEvent(String tenantId, int page, int rp, String resId, Date begin, Date end) {
		MinePageEvent minePageEvent = new MinePageEvent();
		List<Event>events = new ArrayList<>();
		Event event = new Event();
		event.setMsgContent("msgContent");
		Date occurTime = new Date();
		event.setOccurTime(occurTime);
		event.setServerity((short)10);
		events.add(event);
		minePageEvent.setEvents(events);
		return minePageEvent;
	}

	@Override
	public PageUnrecoveredEvent getUnrecoveredEvents(String tenantId, int currentPage, int pageSize, String key,
			String searchValue, String sort) {
		PageUnrecoveredEvent pageUnrecoveredEvent = new PageUnrecoveredEvent();
		pageUnrecoveredEvent.setCurrentPage(1);
		pageUnrecoveredEvent.setPageSize(10);
		pageUnrecoveredEvent.setTotal(1);
		List<UnrecoveredEvent> list = new ArrayList<>();
		UnrecoveredEvent event = new UnrecoveredEvent("e0a67e986a594a61b3d1e523a0a39c77", new Date(), (short) 3,
				"e0a67e986a594a61b3d1e523a0a39c77", "test", "test", "test", "e0a67e986a594a61b3d1e523a0a39c77", "10.1.10.1",
				"tt:tt","e0a67e986a594a61b3d1e523a0a39c77","e0a67e986a594a61b3d1e523a0a39c77");
		pageUnrecoveredEvent.setLists(list);
		return pageUnrecoveredEvent;
	}

	@Override
	public boolean updateEventsByOldResId(String tenantId, String oldResId, String newResId) {
		return false;
	}

	@Override
	public PageResEvent getAlertResEvents(String tenantId, String resourceId, int currentPage, int pageSize) {
		PageResEvent pageResEvent = new PageResEvent();
		pageResEvent.setCurrentPage(1);
		pageResEvent.setPageSize(10);
		pageResEvent.setTotal(1);
		List<ResEvent> list = new ArrayList<>();
		ResEvent event = new ResEvent("e0a67e986a594a61b3d2e523a0a39c77", (short) 3,
				"e0a67e986a594a61b3d3e523a0a39c77", "e0a67e986a594a61b3d12523a0a39c77", "10.1.10.1",
				new Date(), "test");
		pageResEvent.setLists(list);
		return pageResEvent;
	}

}
