package uyun.bat.event.api.logic;

import uyun.bat.event.api.entity.*;

import java.util.Date;
import java.util.List;

public interface EventLogic {

	PageEvent searchEvent(String tenantId, int currentPage, int pageSize, String searchValue, String serverity,
			long beginTime, long endTime, Integer granularity);

	Event create(Event event);

	Event getEventById(String id);

	EventFault getFaultById(String id);

	long create(List<Event> events);

	EventGraphData searchEventGraphData(String tenantId, String searchValue, long beginTime, long endTime,
			int granularity);

	PageEvent getEventsByFaultId(String tenantId, String eventId, String faultId, int current, int pageSize);

	List<EventMonitorData> queryMatchedMonitorData(String tenantId, Short[] sourceTypes, Short[] serveritys,
			String keyWords, String[] tags, Date beginTime, Date endTime);

	int deleteByTime(Date beginTime, Date endTime);

	List<String> getTagsByEventId(String eventId);

	int getEventCount(String tenantId,Date begin,Date end);

	long getInsertAtomic();

	long getEventPageAtomic();

	long getEventGraphAtomic();

	long getInsertFailedAtomic();

	long getQueryFailedAtomic();

	List<EventSpanTime> getEventSpanTime();

	MinePageEvent searchEvent(String tenantId, int currentPage, int pageSize, String resId, Date begin, Date end);

	boolean delete(String tenantId, String resourceId);

	/**
	 * 根据资源变更的内容更新之前存储的事件内容
	 * */
	boolean updateEventsByResTags(String tenantId, String resourceId, String resTags);

	/**
	 * 根据总览的key:value获取未恢复的告警和错误事件列表
	 * searchValue可查ip hostname msgcontent
	 * */
	PageUnrecoveredEvent getUnrecoveredEvents(String tenantId, int currentPage, int pageSize, String key,
			String searchValue, String sort);

	/**
	 * 监测器涉及到触发条件的修改会将检查点清除 事件这边要发送一条恢复事件
	 * */
	boolean updateEventTypeByMonitorId(String tenantId,String monitorId);

	/**
	 * 统一资源库由资源oldId更新事件为资源newId
	 * */
	boolean updateEventsByOldResId(String tenantId, String oldResId, String newResId);

	/**
	 * 根据资源id查询告警条件及信息
	 */
	PageResEvent getAlertResEvents(String tenantId, String resourceId, int currentPage, int pageSize);
}
