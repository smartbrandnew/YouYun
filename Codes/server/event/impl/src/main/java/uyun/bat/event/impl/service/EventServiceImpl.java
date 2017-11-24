package uyun.bat.event.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import uyun.bat.event.api.entity.*;
import uyun.bat.event.api.logic.EventLogic;
import uyun.bat.event.api.service.EventService;

import java.util.Date;
import java.util.List;

@Service(protocol = "dubbo")
public class EventServiceImpl implements EventService {

	@Autowired
	private EventLogic eventLogic;

	public EventGraphData searchEventGraphData(String tenantId, String searchValue, long beginTime, long endTime,
			int granularity) {

		return eventLogic.searchEventGraphData(tenantId,searchValue,beginTime,endTime,granularity);
	}

	public PageEvent searchEvent(String tenantId, int currentPage, int pageSize, String searchValue, String serverity,
			long beginTime, long endTime, Integer granularity) {
		return eventLogic.searchEvent(tenantId, currentPage, pageSize, searchValue, serverity,beginTime, endTime,granularity);
	}

	public Event create(Event event) {
		eventLogic.create(event);
		return event;
	}

	public long create(List<Event> events) {
		long count = 0l;
		if (null == events || events.size() < 1) {
			return count;
		}
		return eventLogic.create(events);
	}

	public PageEvent getEventsByFaultId(String tenantId, String eventId, String faultId, int current, int pageSize) {
		return eventLogic.getEventsByFaultId(tenantId,eventId,faultId,current,pageSize);
	}

	public List<EventMonitorData> queryMatchedMonitorData(String tenantId, Short[] sourceTypes, Short[] serveritys,
			String keyWords, String[] tags, Date beginTime, Date endTime) {
		return eventLogic.queryMatchedMonitorData(tenantId, sourceTypes, serveritys, keyWords, tags, beginTime,
				endTime);
	}

	public List<String> getTagsByEventId(String eventId) {
		return eventLogic.getTagsByEventId(eventId);
	}

	@Override
	public int getEventCount(String tenantId, Date begin, Date end) {
		return eventLogic.getEventCount(tenantId,begin,end);
	}

	@Override
	public MinePageEvent searchEvent(String tenantId, int currentPage, int pageSize, String resId, Date begin, Date end) {
		return eventLogic.searchEvent(tenantId,currentPage,pageSize,resId,begin,end);
	}

	@Override
	public PageUnrecoveredEvent getUnrecoveredEvents(String tenantId, int currentPage, int pageSize, String key,
			String searchValue, String sort) {
		return eventLogic.getUnrecoveredEvents(tenantId, currentPage, pageSize, key, searchValue, sort);
	}

	@Override
	public boolean updateEventsByOldResId(String tenantId, String oldResId, String newResId) {
		return eventLogic.updateEventsByOldResId(tenantId, oldResId, newResId);
	}

	@Override
	public PageResEvent getAlertResEvents(String tenantId, String resourceId, int currentPage, int pageSize) {
		return eventLogic.getAlertResEvents(tenantId, resourceId, currentPage, pageSize);
	}
}
